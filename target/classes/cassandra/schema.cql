-- =============================================================================
-- Rental Property Manager – Cassandra Schema
-- Spring Boot 4 / Spring Data Cassandra (Reactive)
--
-- Run against an existing keyspace, or create it first:
--   CREATE KEYSPACE IF NOT EXISTS rental_manager
--     WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
--
-- Note: Spring Boot auto-creates tables when schema-action=create_if_not_exists
-- is set in application.yml. This file serves as documentation and can be used
-- for manual provisioning or CI/CD pipelines.
-- =============================================================================

-- ── PROPERTY Bounded Context ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS properties (
    id         uuid PRIMARY KEY,
    owner_id   uuid,
    name       text,
    street     text,
    city       text,
    state      text,
    zip_code   text,
    country    text,
    type       text,
    created_at timestamp
);

CREATE INDEX IF NOT EXISTS idx_properties_owner ON properties (owner_id);

CREATE TABLE IF NOT EXISTS property_units (
    id                  uuid PRIMARY KEY,
    property_id         uuid,
    unit_number         text,
    bedrooms            int,
    bathrooms           int,
    square_footage      double,
    monthly_rent_amount decimal,
    currency_code       text,
    status              text
);

CREATE INDEX IF NOT EXISTS idx_units_property ON property_units (property_id);
CREATE INDEX IF NOT EXISTS idx_units_status   ON property_units (status);

-- ── TENANT Bounded Context ────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tenants (
    id            uuid PRIMARY KEY,
    first_name    text,
    last_name     text,
    national_id   text,
    email         text,
    phone_number  text,
    credit_score  int,
    status        text,
    registered_at timestamp
);

CREATE INDEX IF NOT EXISTS idx_tenants_email  ON tenants (email);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants (status);

-- ── LEASING Bounded Context ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS leases (
    id                 uuid PRIMARY KEY,
    tenant_id          uuid,
    property_id        uuid,
    unit_id            uuid,
    start_date         date,
    end_date           date,
    monthly_rent       decimal,
    security_deposit   decimal,
    status             text,
    termination_reason text,
    created_at         timestamp
);

CREATE INDEX IF NOT EXISTS idx_leases_tenant ON leases (tenant_id);
CREATE INDEX IF NOT EXISTS idx_leases_unit   ON leases (unit_id);
CREATE INDEX IF NOT EXISTS idx_leases_status ON leases (status);

-- ── PAYMENT Bounded Context ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS payments (
    id            uuid PRIMARY KEY,
    lease_id      uuid,
    tenant_id     uuid,
    amount_due    decimal,
    amount_paid   decimal,
    currency_code text,
    due_date      date,
    paid_date     date,
    status        text,
    type          text,
    created_at    timestamp
);

CREATE INDEX IF NOT EXISTS idx_payments_lease  ON payments (lease_id);
CREATE INDEX IF NOT EXISTS idx_payments_tenant ON payments (tenant_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments (status);

-- ── MAINTENANCE Bounded Context ───────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS maintenance_requests (
    id                  uuid PRIMARY KEY,
    property_id         uuid,
    unit_id             uuid,
    tenant_id           uuid,
    problem_description text,
    resolution_notes    text,
    priority            text,
    status              text,
    requested_at        timestamp,
    completed_at        timestamp
);

CREATE INDEX IF NOT EXISTS idx_maintenance_property ON maintenance_requests (property_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_tenant   ON maintenance_requests (tenant_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_status   ON maintenance_requests (status);
