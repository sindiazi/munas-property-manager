# Munas Property Manager

A production-grade **rental property management system** built with Domain-Driven Design (DDD) and Hexagonal Architecture. It covers the full rental lifecycle: property setup, tenant registration, lease management, rent payment tracking, and maintenance request handling — all on a fully reactive, non-blocking stack.

---

## What It Does

The system models five independent bounded contexts:

| Context | Responsibility |
|---|---|
| **Property** | Manage properties, rentable units (status, availability), and unit room galleries |
| **Tenant** | Register and manage tenant profiles and activation status |
| **Leasing** | Handle lease agreement lifecycle (DRAFT → ACTIVE → EXPIRED/TERMINATED) |
| **Payment** | Track rent payments with partial payment support and overdue detection |
| **Maintenance** | Manage maintenance requests and the reference catalogue of categories and issue templates |

Bounded contexts communicate exclusively through domain events — there are no direct object references across context boundaries, only UUID references.

---

## Architecture

### Hexagonal (Ports & Adapters)

Each bounded context is structured as:

```
{context}/
├── domain/
│   ├── aggregate/        # Aggregate roots with business rules
│   ├── event/            # Immutable domain events (sealed interface)
│   ├── repository/       # Output port interfaces
│   ├── service/          # Pure domain services
│   └── valueobject/      # Immutable value objects (Java records)
├── application/
│   ├── port/
│   │   ├── input/        # Use case interfaces (primary ports)
│   │   └── output/       # Persistence port interfaces
│   ├── service/          # Application services (orchestration + event publishing)
│   └── dto/
│       ├── command/      # Inbound request DTOs
│       └── response/     # Outbound response DTOs
└── infrastructure/
    ├── persistence/
    │   ├── adapter/      # Implements output ports via Spring Data
    │   ├── entity/       # Cassandra entities
    │   ├── mapper/       # Domain ↔ Entity mapping (MapStruct)
    │   └── repository/   # Spring Data Reactive Cassandra repositories
    └── web/
        └── controller/   # REST endpoints (primary adapters)
```

### Request Flow

```
REST Controller
  → Input Port (Use Case Interface)
  → Application Service (orchestrate + publish events)
  → Domain Aggregate (business rules + domain events)
  → Output Port (repository interface)
  → Persistence Adapter (Spring Data Cassandra)
  → Apache Cassandra
```

### Domain Events

All domain events are immutable `record` types implementing a `sealed DomainEvent` interface. Events are published by the application service after successful persistence. This enables exhaustive pattern matching and provides a foundation for event sourcing or event-driven cross-context workflows.

Examples: `LeaseActivatedEvent`, `LeaseTerminatedEvent`, `LeaseExpiredEvent`, `PaymentReceivedEvent`, `MaintenanceRequestStatusChangedEvent`, `MaintenanceCategoryCreatedEvent`, `MaintenanceIssueTemplateAddedEvent`

### CQRS Read Projections

The leasing context uses a CQRS split for occupancy queries. `LeaseProjectionHandler` listens for domain events and maintains two denormalised Cassandra tables:

| Table | Partition Key | Purpose |
|---|---|---|
| `tenant_occupied_unit` | `tenant_id` | O(1) lookup — which unit is a tenant occupying right now? |
| `unit_rental_history` | `unit_id` | Single-partition scan — full rental history for a unit, newest first |

Query endpoints:
- `GET /api/v1/occupancy/tenant/{tenantId}` — current occupancy for a tenant
- `GET /api/v1/occupancy/unit/{unitId}/history` — rental history for a unit

---

## Project Structure

```
src/main/java/com/example/rentalmanager/
├── property/
├── tenant/
├── leasing/
│   └── infrastructure/projection/   # LeaseProjectionHandler (CQRS event listener)
├── payment/
├── maintenance/
└── shared/
    ├── domain/          # AggregateRoot base class, DomainEvent sealed interface
    └── infrastructure/
        ├── web/         # RequestLoggingFilter, GlobalExceptionHandler
        ├── config/      # ServiceLoggingAspect (AOP)
        ├── security/    # JwtFilter, SsnEncryptionService
        └── seed/        # DataSeeder (@Profile("seed"))
```

```
src/main/resources/
├── application.yml
├── application.conf      # DataStax driver config (Cassandra request logging)
└── cassandra/
    └── schema.cql        # Cassandra keyspace and table definitions
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 25 (with `--enable-preview`) |
| Framework | Spring Boot 4.0 |
| Web | Spring WebFlux (reactive, Netty) |
| Database | Apache Cassandra (reactive driver) |
| Persistence | Spring Data Reactive Cassandra |
| Security | Spring Security WebFlux, JWT (JJWT 0.12.6) |
| Code generation | Lombok 1.18, MapStruct 1.6 |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Observability | Actuator, Micrometer, Prometheus, AOP method logging |
| Testing | Spring Boot Test, Reactor Test, Testcontainers, ArchUnit |
| Build | Maven |

---

## Running Locally

**Prerequisites:** Java 25, Maven, Docker (for Cassandra)

> **Note:** Java 25 must be set explicitly. Prefix all `mvnw` commands with `JAVA_HOME`:
> ```bash
> JAVA_HOME="C:/Program Files/Java/jdk-25" ./mvnw spring-boot:run
> ```

```bash
# Start Cassandra
docker run -d -p 9042:9042 --name cassandra cassandra:latest

# Run the application
JAVA_HOME="C:/Program Files/Java/jdk-25" ./mvnw spring-boot:run

# Seed the database with sample data (truncates & re-inserts on every run)
JAVA_HOME="C:/Program Files/Java/jdk-25" ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

The schema is created automatically on startup (`create_if_not_exists`).

**Endpoints:**
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI docs: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/prometheus`

**Default credentials:** `admin` / `Admin@1234`

**M-Pesa environment variables** (required for STK Push):
```bash
MPESA_CONSUMER_KEY=<Daraja app consumer key>
MPESA_CONSUMER_SECRET=<Daraja app consumer secret>
MPESA_SHORT_CODE=<PayBill shortcode, default: 174379 sandbox>
MPESA_PASSKEY=<Daraja passkey>
MPESA_CALLBACK_URL=https://<ngrok-or-public-url>/api/v1/payments/mpesa/callback
MPESA_BASE_URL=https://sandbox.safaricom.co.ke   # or https://api.safaricom.co.ke for production
```

---

## API Overview

### Tenants
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/tenants` | Register a new tenant |
| `GET` | `/api/v1/tenants` | List all tenants |
| `GET` | `/api/v1/tenants/{id}` | Get tenant by ID |
| `PATCH` | `/api/v1/tenants/{id}/activate` | Activate an inactive tenant |

### Properties
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/properties` | Create a property |
| `GET` | `/api/v1/properties` | List all properties |
| `GET` | `/api/v1/properties/{id}` | Get property by ID |
| `PUT` | `/api/v1/properties/{id}` | Update property details |
| `POST` | `/api/v1/properties/{id}/units` | Add a unit to a property |

### Leasing
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/leases` | Create a lease (accepts tenant UUID or 9-digit National ID) |
| `GET` | `/api/v1/leases` | List all leases |
| `GET` | `/api/v1/leases/{id}` | Get lease by ID |
| `PATCH` | `/api/v1/leases/{id}/activate` | Activate a draft lease |
| `PATCH` | `/api/v1/leases/{id}/terminate` | Terminate an active lease |

### Occupancy (CQRS read side)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/occupancy/tenant/{tenantId}` | Current unit for a tenant |
| `GET` | `/api/v1/occupancy/unit/{unitId}/history` | Full rental history for a unit |

### Unit Room Gallery
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/units/{unitId}/rooms` | Get all rooms with images for a unit |
| `POST` | `/api/v1/units/{unitId}/rooms` | Add a room to a unit |
| `PUT` | `/api/v1/units/{unitId}/rooms/{roomId}` | Update a room |
| `DELETE` | `/api/v1/units/{unitId}/rooms/{roomId}` | Remove a room and its images |
| `POST` | `/api/v1/units/{unitId}/rooms/{roomId}/images` | Add an image to a room |
| `PUT` | `/api/v1/units/{unitId}/rooms/{roomId}/images/{imageId}` | Update a room image |
| `DELETE` | `/api/v1/units/{unitId}/rooms/{roomId}/images/{imageId}` | Remove a room image |

Room types: `LIVING_ROOM`, `BEDROOM`, `KITCHEN`, `BATHROOM`, `FLOOR_PLAN`. Write operations require `ADMIN` or `PROPERTY_MANAGER` role.

### Maintenance Categories (reference data)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/maintenance/categories` | List all categories with issue templates |
| `GET` | `/api/v1/maintenance/categories/{id}` | Get a single category |
| `POST` | `/api/v1/maintenance/categories` | Create a category |
| `PUT` | `/api/v1/maintenance/categories/{id}` | Rename a category |
| `DELETE` | `/api/v1/maintenance/categories/{id}` | Delete a category and all its issue templates |
| `POST` | `/api/v1/maintenance/categories/{id}/issues` | Add an issue template to a category |
| `PUT` | `/api/v1/maintenance/categories/{id}/issues/{issueId}` | Update an issue template |
| `DELETE` | `/api/v1/maintenance/categories/{id}/issues/{issueId}` | Remove an issue template |

Category and issue IDs are human-readable slugs (e.g. `plumbing`, `leaking_faucet`). Write operations require `ADMIN` or `PROPERTY_MANAGER` role. Reads are open to all authenticated users.

### Payments
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/payments` | JWT | Create a payment record |
| `GET` | `/api/v1/payments` | JWT | List all payments |
| `GET` | `/api/v1/payments/{id}` | JWT | Get payment by ID |
| `PATCH` | `/api/v1/payments/{id}/receive` | JWT | Manually record a payment |

#### M-Pesa STK Push
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/payments/mpesa` | JWT (ADMIN/PM) | Initiate STK Push — creates payment + triggers Daraja prompt on customer's phone; returns 202 with `checkoutRequestId` |
| `POST` | `/api/v1/payments/mpesa/callback` | **None** | Daraja posts callback here on success/failure; always returns 200 |
| `GET` | `/api/v1/payments/{id}/mpesa/status` | JWT | Poll Daraja Query API for live transaction status |

### Maintenance
Full CRUD via `/api/v1/maintenance`. See Swagger UI for details.

---

## Key Design Decisions

- **Reactive all the way**: WebFlux + Reactive Cassandra driver — no blocking calls anywhere in the stack.
- **UUID cross-context references**: Bounded contexts reference each other only by UUID, preventing transitive coupling.
- **One non-terminal lease per unit**: Creating a lease is rejected if the unit already has a DRAFT or ACTIVE lease, enforcing the invariant that a unit can only be leased to one tenant at a time.
- **Sealed domain events**: Prevents unauthorized event types; enables exhaustive `switch` matching.
- **Events published post-persistence**: Application services publish events only after a successful write — ready for transactional outbox pattern.
- **CQRS projections via event listeners**: Read-optimised Cassandra tables are maintained by `@EventListener` handlers reacting to domain events (fire-and-forget, eventual consistency).
- **National ID encryption at rest**: Tenant National ID numbers are stored AES-256/CBC encrypted. A separate HMAC-SHA-256 hash column enables equality lookups without decryption. Lease creation accepts either a tenant UUID or a plain 9-digit National ID number.
- **ArchUnit tests**: Architecture constraints (e.g. domain layer must not depend on infrastructure) are enforced at compile/test time.
- **RFC 9457 error responses**: All exceptions are mapped to standardized `ProblemDetail` responses via a global handler.

---

## Observability

| Layer | Mechanism |
|---|---|
| HTTP requests | `RequestLoggingFilter` (WebFilter, order -100) — logs method, path, status, and latency |
| Service methods | `ServiceLoggingAspect` (AOP `@Around`) — logs entry, success, and error for all application services |
| Cassandra queries | DataStax `RequestLogger` — logs every CQL statement with bound values and latency |
| Metrics | Micrometer + Prometheus (`/actuator/prometheus`) |

---

## Sample Data

The `seed` profile loads:
- **2 Nairobi properties**: Kilimani Court (12 units) and Westlands Residences (10 units)
- **20 Kenyan tenants** with encrypted National ID numbers
- **3 years of leases**: Year 1 & 2 EXPIRED, Year 3 ACTIVE (15 tenants)
- **Monthly payments** with realistic outcomes (78% on-time, 12% late, 5% partial, 3% overdue, 2% cancelled)
- **~30 maintenance requests** spread across 2 years
- **CQRS projection rows** for current occupancy and rental history
- **10 maintenance categories** with 41 issue templates (each category includes a catch-all "Other" issue)

Units 0–14 are OCCUPIED; units 15–21 are AVAILABLE.

The seeder is idempotent: it truncates leases, payments, maintenance requests, projection tables, and maintenance reference data before each run. Properties, units, and tenants use deterministic UUIDs and upsert safely.

**Default admin settings** (seeded by `DataInitializer` on first startup): theme `DARK`, currency `KES`, timezone `Africa/Nairobi`.
