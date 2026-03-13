-- =============================================================================
-- Rental Property Manager – Initial Schema
-- Spring Boot 4 / Spring Data R2DBC / PostgreSQL
-- =============================================================================

-- ── PROPERTY Bounded Context ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS properties (
    id          UUID         PRIMARY KEY,
    owner_id    UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    zip_code    VARCHAR(20)  NOT NULL,
    country     VARCHAR(100) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_properties_owner ON properties (owner_id);

CREATE TABLE IF NOT EXISTS property_units (
    id                  UUID           PRIMARY KEY,
    property_id         UUID           NOT NULL REFERENCES properties (id) ON DELETE CASCADE,
    unit_number         VARCHAR(50)    NOT NULL,
    bedrooms            INT            NOT NULL DEFAULT 0,
    bathrooms           INT            NOT NULL DEFAULT 0,
    square_footage      DOUBLE PRECISION NOT NULL,
    monthly_rent_amount NUMERIC(12, 2) NOT NULL,
    currency_code       VARCHAR(3)     NOT NULL DEFAULT 'USD',
    status              VARCHAR(30)    NOT NULL DEFAULT 'AVAILABLE',
    UNIQUE (property_id, unit_number)
);

CREATE INDEX idx_units_property ON property_units (property_id);
CREATE INDEX idx_units_status   ON property_units (status);

-- ── TENANT Bounded Context ────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tenants (
    id            UUID         PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    national_id   VARCHAR(50),
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone_number  VARCHAR(30)  NOT NULL,
    credit_score  INT          NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'INACTIVE',
    registered_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_email  ON tenants (email);
CREATE INDEX idx_tenants_status ON tenants (status);

-- ── LEASING Bounded Context ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS leases (
    id                  UUID           PRIMARY KEY,
    tenant_id           UUID           NOT NULL,
    property_id         UUID           NOT NULL,
    unit_id             UUID           NOT NULL,
    start_date          DATE           NOT NULL,
    end_date            DATE           NOT NULL,
    monthly_rent        NUMERIC(12, 2) NOT NULL,
    security_deposit    NUMERIC(12, 2) NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    termination_reason  TEXT,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_lease_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_leases_tenant     ON leases (tenant_id);
CREATE INDEX idx_leases_unit       ON leases (unit_id);
CREATE INDEX idx_leases_status     ON leases (status);
CREATE INDEX idx_leases_unit_active ON leases (unit_id, status);

-- ── PAYMENT Bounded Context ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS payments (
    id            UUID           PRIMARY KEY,
    lease_id      UUID           NOT NULL,
    tenant_id     UUID           NOT NULL,
    amount_due    NUMERIC(12, 2) NOT NULL,
    amount_paid   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    currency_code VARCHAR(3)     NOT NULL DEFAULT 'USD',
    due_date      DATE           NOT NULL,
    paid_date     DATE,
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    type          VARCHAR(30)    NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_lease  ON payments (lease_id);
CREATE INDEX idx_payments_tenant ON payments (tenant_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_due    ON payments (due_date) WHERE status IN ('PENDING', 'PARTIALLY_PAID');

-- ── MAINTENANCE Bounded Context ───────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS maintenance_requests (
    id                   UUID        PRIMARY KEY,
    property_id          UUID        NOT NULL,
    unit_id              UUID        NOT NULL,
    tenant_id            UUID        NOT NULL,
    problem_description  TEXT        NOT NULL,
    resolution_notes     TEXT,
    priority             VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status               VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    requested_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at         TIMESTAMPTZ
);

CREATE INDEX idx_maintenance_property ON maintenance_requests (property_id);
CREATE INDEX idx_maintenance_tenant   ON maintenance_requests (tenant_id);
CREATE INDEX idx_maintenance_status   ON maintenance_requests (status);
CREATE INDEX idx_maintenance_priority ON maintenance_requests (priority) WHERE status = 'OPEN';
