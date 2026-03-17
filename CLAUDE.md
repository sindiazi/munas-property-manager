# Rental Property Manager — CLAUDE.md

## Project overview

Spring Boot 4 / WebFlux backend for a rental property management system.
Frontend lives at `../munas-property-manager-ui` (Next.js, Radix UI).

Architecture: **DDD + Hexagonal (Ports & Adapters)** across 5 bounded contexts.
Database: **Apache Cassandra** (Spring Data Cassandra Reactive).
Reactive stack: **Project Reactor** (Mono / Flux) throughout — no blocking I/O anywhere.

---

## Running the backend

Java 25 is required. Always prefix Maven commands with the explicit `JAVA_HOME`:

```bash
# Normal start
JAVA_HOME="C:/Program Files/Java/jdk-25" ./mvnw spring-boot:run

# Seed database (truncates non-idempotent tables then re-inserts)
JAVA_HOME="C:/Program Files/Java/jdk-25" ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed

# Run tests
JAVA_HOME="C:/Program Files/Java/jdk-25" ./mvnw test
```

Backend runs on `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

Kill a stale process on port 8080:
```bash
netstat -ano | grep :8080 | grep LISTEN   # find PID
taskkill //PID <pid> //F
```

---

## Bounded contexts

| Context | Package | Key aggregate |
|---|---|---|
| Property | `property` | `Property`, `PropertyUnit`, `UnitRoom` |
| Tenant | `tenant` | `Tenant` |
| Leasing | `leasing` | `Lease` |
| Payment | `payment` | `Payment` |
| Maintenance | `maintenance` | `MaintenanceRequest`, `MaintenanceCategory` |

Each context follows the same internal layout:
```
<context>/
  domain/
    aggregate/        # Aggregate root (extends AggregateRoot)
    valueobject/      # Immutable value objects
    event/            # Domain events
    service/          # Domain services (pure logic, no I/O)
    repository/       # Domain repository interface (not used directly)
  application/
    port/input/       # Use case interfaces (primary ports)
    port/output/      # Persistence/external ports (secondary ports)
    service/          # Application service implementing use cases
    dto/command/      # Inbound command records
    dto/response/     # Outbound response records
  infrastructure/
    persistence/
      entity/         # Spring Data Cassandra @Table entities
      repository/     # ReactiveCassandraRepository interfaces
      adapter/        # Secondary adapter implementing output port
      mapper/         # MapStruct mapper (entity ↔ domain)
    web/controller/   # Primary adapter (@RestController)
  config/             # Bean configuration (@Configuration)
```

---

## Architecture rules (enforced by ArchUnit)

- Domain layer must not depend on infrastructure or application layers.
- Application layer must not depend on infrastructure layer.
- Controllers must only call use case interfaces (input ports), never services directly.
- Adapters implement output ports; they must not be called from application layer directly.

Test: `src/test/java/.../architecture/HexagonalArchitectureTest.java`

---

## Event-Driven Architecture (EDA) principle

**Rule: cross-domain state changes must go through domain events, never direct calls.**

When two bounded contexts need to stay in sync:
1. The domain that **owns the state** mutates its own aggregate and emits a domain event.
2. The **subscribing domain** listens for that event in its own infrastructure layer and updates its own state to stay eventually consistent.
3. No application service in domain A may call an application service or output port in domain B to mutate domain B's state.

### Event publishing abstraction

All application services publish events through `DomainEventPublisher` (in `shared/domain`) rather than Spring's `ApplicationEventPublisher` directly. The default implementation `SpringDomainEventPublisher` (in `shared/infrastructure/event`) delegates to Spring's in-process event bus. To swap to Kafka or another broker: provide a new `DomainEventPublisher` bean — no application-layer code changes.

```
shared/domain/DomainEventPublisher          ← interface (domain layer)
shared/infrastructure/event/
  SpringDomainEventPublisher                ← Spring in-process impl (swap for Kafka impl here)
```

### Event subscribers (handlers)

Handlers live in `infrastructure/event/` within the subscribing bounded context. They use `@EventListener` for the Spring in-process bus (swapped to `@KafkaListener` etc. when moving to a broker).

| Handler | Location | Listens to | Effect |
|---|---|---|---|
| `UnitStatusSyncHandler` | `property/infrastructure/event/` | `LeaseActivatedEvent`, `LeaseTerminatedEvent`, `LeaseExpiredEvent` | Updates `PropertyUnit` status (OCCUPIED / AVAILABLE) |
| `LeaseProjectionHandler` | `leasing/infrastructure/projection/` | `LeaseActivatedEvent`, `LeaseTerminatedEvent`, `LeaseExpiredEvent` | Maintains CQRS read projections |

All handlers use fire-and-forget (`.subscribe()`) — cross-domain state sync is eventually consistent by design.

### What this replaced

`UnitStatusPort` (an output port in the Leasing context pointing into Property) and `UnitStatusAdapter` have been deleted. `LeaseApplicationService` no longer calls into the Property domain directly after activating or terminating a lease.

### Lease expiry policy

`LeaseExpiryPolicy` (`leasing/application/service/`) is a `@Scheduled` job running daily at 01:00. It finds all `ACTIVE` leases past their end date, calls `lease.expire()`, and publishes `LeaseExpiredEvent`. The event then flows to `UnitStatusSyncHandler` and `LeaseProjectionHandler` via the normal EDA path.

---

## Database

**Cassandra** keyspace: `rental_manager` (default; override via `CASSANDRA_KEYSPACE` env var).

Schema reference: `src/main/resources/cassandra/schema.cql`
Spring auto-creates tables (`schema-action: create_if_not_exists`).

### CQRS read projections (leasing context)

Two denormalised projection tables maintained by `LeaseProjectionHandler` (`@EventListener`):

| Table | PK | Purpose |
|---|---|---|
| `tenant_occupied_unit` | `tenant_id` | O(1) lookup: which unit is a tenant in right now? |
| `unit_rental_history` | `(unit_id)`, cluster: `lease_start DESC` | Full rental history for a unit, newest first |

`DataSeeder` backfills both on seed runs.

### Important Cassandra mapping rule

Spring Data Cassandra maps `camelCase` field names to **all-lowercase** column names by default (e.g. `tenantId` → `tenantid`). All snake_case columns **must** have an explicit `@Column("snake_case_name")` annotation on the entity field.

### Composite primary key pattern

When a table is partitioned by a foreign-key column (e.g. `maintenance_issue_templates` partitioned by `category_id`), use a `@PrimaryKeyClass`:

```java
@PrimaryKeyClass
public class MaintenanceIssueTemplatePK implements Serializable {
    @PrimaryKeyColumn(name = "category_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String categoryId;
    @PrimaryKeyColumn(name = "id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String id;
}
```

Repository derived queries then use the nested field name: `findByKeyCategoryId(String categoryId)`, `deleteByKeyCategoryId(String categoryId)`. This enables single-partition bulk reads and deletes without `ALLOW FILTERING`.

### Manual mapper rule

Use a plain `@Component` class (not MapStruct `@Mapper`) when domain object assembly requires **more than one entity source** (e.g. assembling `MaintenanceCategory` from a category row plus a list of issue rows). MapStruct `@Mapper` is for straightforward 1:1 entity ↔ domain mappings.

---

## Key domain rules

- **One non-terminal lease per unit**: a unit cannot have a new lease created while an ACTIVE or DRAFT lease already exists for it. Enforced in `LeaseApplicationService.createLease()` via `findNonTerminalLeaseByUnitId()`.
- **Lease lifecycle**: `DRAFT → ACTIVE → EXPIRED | TERMINATED`. Activation emits `LeaseActivatedEvent`; termination emits `LeaseTerminatedEvent`; daily expiry policy emits `LeaseExpiredEvent`. Unit status changes (OCCUPIED / AVAILABLE) are driven by `UnitStatusSyncHandler` reacting to these events.
- **Tenant status**: `INACTIVE → ACTIVE`. Tenants must exist before leases can reference them. `PATCH /api/v1/tenants/{id}/activate` activates an inactive tenant.
- **Maintenance category IDs are slug strings** (e.g. `plumbing`, `leaking_faucet`), not UUIDs. Issue template IDs are unique within a category only — the composite key is `(category_id, id)`.
- **Unit room display order** auto-resolves to the current count when not explicitly provided (append to end). Same pattern applies to room image display order.

---

## National ID numbers

Tenant National ID numbers (`national_id_no`) are 9-digit strings.

- Stored **encrypted** (AES-256/CBC): `base64(IV[16] || ciphertext)`.
- A separate `national_id_no_hash` column stores an HMAC-SHA-256 of the digits for equality lookups without decryption.
- `SsnEncryptionService` handles encrypt / decrypt / mask / computeLookupHash.
- Lease creation accepts either a tenant UUID or a plain 9-digit National ID — `LeaseApplicationService.resolveTenantId()` detects and resolves the latter.
- Encryption key: `security.ssn-encryption-key` in `application.yml` (override via `SSN_ENCRYPTION_KEY` env var in production).

---

## Security

- Spring Security WebFlux with JWT (JJWT 0.12.6).
- JWT secret: `jwt.secret` (override via `JWT_SECRET` env var).
- Token expiry: 24 h by default (`jwt.expiration-ms`).
- Default admin credentials: `admin` / `Admin@1234` (overridable via `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` env vars).

---

## Observability

- **HTTP layer**: `RequestLoggingFilter` (order -100) logs `→ METHOD /path` and `← STATUS METHOD /path (Nms)`.
- **Service layer**: `ServiceLoggingAspect` (AOP `@Around`) attaches reactive hooks to log entry, success, and error for all `*.application.service.*` methods.
- **Cassandra**: DataStax `RequestLogger` enabled in `src/main/resources/application.conf`; log level set in `application.yml`.
- Actuator endpoints exposed: `health`, `info`, `prometheus`, `metrics`.

---

## Data seeder

There are two distinct seeders with different purposes:

### `DataInitializer` (`shared/infrastructure/config/`)
Runs on **every startup** via `@EventListener(ApplicationReadyEvent.class)`. Seeds the admin user and their default settings if the admin username does not yet exist. Idempotent — safe to leave active in any environment.

- Creates admin user (`admin` / `Admin@1234` by default; overridable via env vars)
- Seeds admin settings: theme `DARK`, currency `KES`, timezone `Africa/Nairobi`

### `DataSeeder` (`@Profile("seed")`)
Runs only when the `seed` profile is active. **Destructive** — truncates several tables before inserting. Use for dev/demo environments only.

Seeds:
- 2 Nairobi properties, 22 units, 20 Kenyan tenants
- 3 years of leases (Year 1 & 2: EXPIRED, Year 3: ACTIVE for 15 tenants)
- Monthly payments with realistic probabilistic outcomes
- ~30 maintenance requests
- Occupancy and rental history projection rows
- 10 maintenance categories with 41 issue templates (each category includes a catch-all "Other" issue)

**Idempotency**: truncates `leases`, `payments`, `maintenance_requests`, `tenant_occupied_unit`, `unit_rental_history`, `maintenance_issue_templates`, and `maintenance_categories` before inserting. Properties, units, and tenants use deterministic UUIDs and upsert safely on re-runs.

Simulation anchor date: `2026-03-15`. The seeder `Random` uses a fixed seed (42) for reproducible data.

---

## Dependencies — known gotchas

- **`spring-boot-starter-aop` does not exist in Spring Boot 4.** Use `org.aspectj:aspectjweaver` directly (version managed by BOM).
- **Java 25 preview features** are enabled via `--enable-preview` in both the compiler plugin and Surefire.
- **Lombok must appear before MapStruct** in the annotation processor path order.

---

## Frontend

Located at `../munas-property-manager-ui` (separate repo/directory).
Next.js app with Radix UI components and Lucide icons.
Expects backend at `http://localhost:8080` (configurable via `FRONTEND_URL` on the backend side for CORS).
