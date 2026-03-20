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
| **Billing** | Manage invoices (billing obligations) and payment transactions (cash/M-Pesa/card) with auto-generation from lease events |
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

Examples: `LeaseActivatedEvent`, `LeaseTerminatedEvent`, `LeaseExpiredEvent`, `InvoiceCreatedEvent`, `InvoiceSettledEvent`, `PaymentRecordedEvent`, `MaintenanceRequestStatusChangedEvent`, `MaintenanceCategoryCreatedEvent`, `MaintenanceIssueTemplateAddedEvent`

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
├── billing/
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
MPESA_CALLBACK_URL=https://<ngrok-or-public-url>/api/v1/invoices/payments/mpesa/callback
MPESA_BASE_URL=https://sandbox.safaricom.co.ke   # or https://api.safaricom.co.ke for production

# Production callback URL (requires HTTPS — see note below):
# MPESA_CALLBACK_URL=https://<your-domain>/api/v1/invoices/payments/mpesa/callback
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

### Invoices
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/invoices` | JWT | Create an invoice manually (one-off fees) |
| `GET` | `/api/v1/invoices` | JWT | List all invoices |
| `GET` | `/api/v1/invoices/{id}` | JWT | Get invoice by ID |
| `GET` | `/api/v1/invoices/lease/{leaseId}` | JWT | List invoices for a lease |
| `GET` | `/api/v1/invoices/tenant/{tenantId}` | JWT | List invoices for a tenant |
| `POST` | `/api/v1/invoices/{id}/payments/cash` | JWT | Record a cash payment against an invoice |
| `GET` | `/api/v1/invoices/{id}/payments` | JWT | List payment transactions for an invoice |

Invoices are auto-generated on `LeaseActivatedEvent` (RENT + SECURITY_DEPOSIT) and by a monthly cron policy (1st of each month, idempotent).

#### M-Pesa STK Push
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/invoices/{id}/payments/mpesa` | JWT (ADMIN/PM) | Initiate STK Push — records a pending payment transaction against the invoice; triggers Daraja prompt on customer's phone; returns 202 |
| `POST` | `/api/v1/invoices/payments/mpesa/callback` | **None** | Daraja posts callback here on success/failure; always returns 200 |
| `GET` | `/api/v1/invoices/payments/mpesa/{paymentTransactionId}/status` | JWT | Poll Daraja Query API for live transaction status |

### Maintenance
Full CRUD via `/api/v1/maintenance`. See Swagger UI for details.

---

## Production Deployment

### Infrastructure

The production stack is provisioned with Terraform (`terraform/`) and runs on AWS:

| Component | AWS Service |
|---|---|
| Container runtime | ECS Fargate — `munas-property-manager-prod-cluster` |
| Container registry | ECR — `munas-property-manager-prod` |
| Database | Amazon Keyspaces (managed Apache Cassandra) |
| Load balancer | Application Load Balancer |
| Secrets | AWS Secrets Manager |

**Live URL:** `http://munas-property-manager-prod-alb-113235036.us-east-1.elb.amazonaws.com`

### Normal deployment flow (CI/CD)

Every push to `main` triggers `.github/workflows/ci.yml`:

1. **Test** — runs `./mvnw test` on Java 25 (GitHub-hosted runner)
2. **Build & Push** — builds the Docker image, tags it `sha-<git-sha>` and `latest`, pushes to ECR via OIDC (no long-lived keys)
3. **Deploy** — runs `aws ecs update-service --force-new-deployment` and waits for service stability

Pull requests run **tests only**. Image build and deploy happen only on merges to `main`.

**Required GitHub Secrets:**

| Secret | Value |
|---|---|
| `AWS_REGION` | `us-east-1` |
| `AWS_ACCOUNT_ID` | Your 12-digit AWS account ID |
| `OIDC_ROLE_ARN` | ARN of the IAM role GitHub Actions assumes via OIDC |

### Infrastructure changes (Terraform)

Infrastructure changes are managed by `.github/workflows/infra.yml`:

- PRs touching `terraform/**`: runs `terraform plan` and posts the output as a PR comment
- Merges to `main`: runs `terraform apply`
- Can also be triggered manually via `workflow_dispatch`

To plan/apply locally:

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

### Bootstrap: manually pushing a first image

When the ECR repository is empty (e.g. after a fresh `terraform apply` before CI has run), ECS fails to start with `CannotPullContainerError`. To unblock:

```bash
# 1. Authenticate Docker to ECR
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
    <account-id>.dkr.ecr.us-east-1.amazonaws.com

# 2. Build the image (Maven wrapper must be present in the repo)
docker build -t munas-property-manager-prod:latest .

# 3. Tag and push to ECR
ECR="<account-id>.dkr.ecr.us-east-1.amazonaws.com/munas-property-manager-prod"
docker tag munas-property-manager-prod:latest "$ECR:latest"
docker push "$ECR:latest"

# 4. Force ECS to pick up the new image
aws ecs update-service \
  --cluster munas-property-manager-prod-cluster \
  --service munas-property-manager-prod-service \
  --force-new-deployment \
  --region us-east-1

aws ecs wait services-stable \
  --cluster munas-property-manager-prod-cluster \
  --services munas-property-manager-prod-service \
  --region us-east-1
```

### Secrets Management

Secrets live in AWS Secrets Manager and are injected into the ECS task as environment variables at startup:

| Secret path | Env var | Notes |
|---|---|---|
| `munas-property-manager/prod/JWT_SECRET` | `JWT_SECRET` | Base64-encoded HMAC-SHA-256 signing key |
| `munas-property-manager/prod/SSN_ENCRYPTION_KEY` | `SSN_ENCRYPTION_KEY` | Base64-encoded AES-256 key (must decode to exactly 32 bytes) |
| `munas-property-manager/prod/KEYSPACES_USERNAME` | `KEYSPACES_USERNAME` | Amazon Keyspaces service-specific credentials username |
| `munas-property-manager/prod/KEYSPACES_PASSWORD` | `KEYSPACES_PASSWORD` | Amazon Keyspaces service-specific credentials password |
| `munas-property-manager/prod/MPESA_CONSUMER_KEY` | `MPESA_CONSUMER_KEY` | Daraja app consumer key |
| `munas-property-manager/prod/MPESA_CONSUMER_SECRET` | `MPESA_CONSUMER_SECRET` | Daraja app consumer secret |
| `munas-property-manager/prod/MPESA_SHORT_CODE` | `MPESA_SHORT_CODE` | M-Pesa PayBill short code |
| `munas-property-manager/prod/MPESA_PASSKEY` | `MPESA_PASSKEY` | Daraja passkey |

> **M-Pesa callback URL (production):** Safaricom Daraja requires HTTPS for the callback URL in the live environment. The current ALB only has an HTTP listener, so you must either attach an ACM certificate to an HTTPS listener (requires a custom domain) or put CloudFront in front of the ALB before going live with M-Pesa production credentials. The callback path is always `/api/v1/invoices/payments/mpesa/callback`.

> **Gotcha:** Always store secret values **without trailing whitespace or carriage returns**. Values stored with `\r` or `\n` (common when copy-pasting on Windows) cause `SsnEncryptionService` and JWT validation to crash at startup. Use `printf '%s' "<value>"` when updating via CLI, or trim carefully in the console before saving.

### IAM: Amazon Keyspaces access

The ECS task authenticates to Amazon Keyspaces using SigV4 service-specific credentials for IAM user `munas-keyspaces-migration`. That user must have the `AmazonKeyspacesFullAccess` managed policy attached.

To verify or fix:

```bash
aws iam list-attached-user-policies --user-name munas-keyspaces-migration
# If empty:
aws iam attach-user-policy \
  --user-name munas-keyspaces-migration \
  --policy-arn arn:aws:iam::aws:policy/AmazonKeyspacesFullAccess
```

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
- **Monthly invoices** with realistic outcomes (78% on-time, 12% late, 5% partial, 3% overdue, 2% cancelled) plus matching **payment transaction rows** for PAID and PARTIALLY_PAID invoices (70% M-Pesa, 30% cash)
- **~30 maintenance requests** spread across 2 years
- **CQRS projection rows** for current occupancy and rental history
- **10 maintenance categories** with 41 issue templates (each category includes a catch-all "Other" issue)

Units 0–14 are OCCUPIED; units 15–21 are AVAILABLE.

The seeder is idempotent: it truncates leases, invoices, payment transactions, maintenance requests, projection tables, and maintenance reference data before each run. Properties, units, and tenants use deterministic UUIDs and upsert safely.

**Default admin settings** (seeded by `DataInitializer` on first startup): theme `DARK`, currency `KES`, timezone `Africa/Nairobi`.
