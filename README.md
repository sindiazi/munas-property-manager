# Munas Property Manager

A production-grade **rental property management system** built with Domain-Driven Design (DDD) and Hexagonal Architecture. It covers the full rental lifecycle: property setup, tenant registration, lease management, rent payment tracking, and maintenance request handling — all on a fully reactive, non-blocking stack.

---

## What It Does

The system models five independent bounded contexts:

| Context | Responsibility |
|---|---|
| **Property** | Manage properties and their rentable units (status, availability) |
| **Tenant** | Register and manage tenant profiles and activation status |
| **Leasing** | Handle lease agreement lifecycle (DRAFT → ACTIVE → EXPIRED/TERMINATED) |
| **Payment** | Track rent payments with partial payment support and overdue detection |
| **Maintenance** | Manage maintenance requests from submission through completion |

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

Examples: `LeaseActivatedEvent`, `PaymentReceivedEvent`, `MaintenanceRequestStatusChangedEvent`

---

## Project Structure

```
src/main/java/com/example/rentalmanager/
├── property/
├── tenant/
├── leasing/
├── payment/
├── maintenance/
└── shared/
    ├── domain/          # AggregateRoot base class, DomainEvent sealed interface
    └── infrastructure/  # GlobalExceptionHandler (RFC 9457 ProblemDetail)
```

```
src/main/resources/
├── application.yml
└── cassandra/
    └── schema.cql       # Cassandra keyspace and table definitions
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
| Code generation | Lombok 1.18, MapStruct 1.6 |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Observability | Actuator, Micrometer, Prometheus |
| Testing | Spring Boot Test, Reactor Test, Testcontainers, ArchUnit |
| Build | Maven |

---

## Running Locally

**Prerequisites:** Java 25, Maven, Docker (for Cassandra)

```bash
# Start Cassandra
docker run -d -p 9042:9042 --name cassandra cassandra:latest

# Run the application
./mvnw spring-boot:run
```

The schema is created automatically on startup (`create_if_not_exists`).

**Endpoints:**
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI docs: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/prometheus`

---

## Key Design Decisions

- **Reactive all the way**: WebFlux + Reactive Cassandra driver — no blocking calls anywhere in the stack.
- **UUID cross-context references**: Bounded contexts reference each other only by UUID, preventing transitive coupling.
- **Sealed domain events**: Prevents unauthorized event types; enables exhaustive `switch` matching.
- **Events published post-persistence**: Application services publish events only after a successful write — ready for transactional outbox pattern.
- **ArchUnit tests**: Architecture constraints (e.g. domain layer must not depend on infrastructure) are enforced at compile/test time.
- **RFC 9457 error responses**: All exceptions are mapped to standardized `ProblemDetail` responses via a global handler.