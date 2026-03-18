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

-- ── UNIT ROOM GALLERY (Property Bounded Context) ─────────────────────────────

CREATE TABLE IF NOT EXISTS unit_rooms (
    id            uuid PRIMARY KEY,
    unit_id       uuid,
    type          text,
    label         text,
    display_order int
);

CREATE INDEX IF NOT EXISTS idx_unit_rooms_unit ON unit_rooms (unit_id);

CREATE TABLE IF NOT EXISTS unit_room_images (
    id            uuid PRIMARY KEY,
    room_id       uuid,
    url           text,
    display_order int,
    caption       text,
    uploaded_at   timestamp
);

CREATE INDEX IF NOT EXISTS idx_unit_room_images_room ON unit_room_images (room_id);

-- ── TENANT Bounded Context ────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tenants (
    id               uuid PRIMARY KEY,
    first_name       text,
    last_name        text,
    national_id      text,
    email            text,
    phone_number     text,
    credit_score     int,
    status           text,
    registered_at    timestamp,
    national_id_no      text,    -- AES-256/CBC encrypted: base64( IV[16] || ciphertext )
    national_id_no_hash text     -- HMAC-SHA-256 of normalised digits, used for equality lookups
);

CREATE INDEX IF NOT EXISTS idx_tenants_email              ON tenants (email);
CREATE INDEX IF NOT EXISTS idx_tenants_status             ON tenants (status);
CREATE INDEX IF NOT EXISTS idx_tenants_national_id_no     ON tenants (national_id_no_hash);

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

-- ── BILLING Bounded Context ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS invoices (
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

CREATE INDEX IF NOT EXISTS idx_invoices_lease  ON invoices (lease_id);
CREATE INDEX IF NOT EXISTS idx_invoices_tenant ON invoices (tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices (status);

CREATE TABLE IF NOT EXISTS payments (
    id            uuid PRIMARY KEY,
    invoice_id    uuid,
    tenant_id     uuid,
    amount        decimal,
    currency_code text,
    method        text,
    status        text,
    reference     text,
    payment_date  date,
    created_at    timestamp
);

CREATE INDEX IF NOT EXISTS idx_payments_invoice ON payments (invoice_id);

-- ── MAINTENANCE Bounded Context ───────────────────────────────────────────────

-- ── MAINTENANCE REFERENCE DATA (categories + issue templates) ────────────────

CREATE TABLE IF NOT EXISTS maintenance_categories (
    id   text PRIMARY KEY,
    name text
);

-- Partitioned by category_id so all issues for a category are co-located.
-- This allows a single partition read/delete when loading or replacing a category's issues.
CREATE TABLE IF NOT EXISTS maintenance_issue_templates (
    category_id text,
    id          text,
    title       text,
    description text,
    priority    text,
    PRIMARY KEY (category_id, id)
);

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

-- ── CQRS READ PROJECTIONS (Leasing) ──────────────────────────────────────────
--
-- These tables are the query-side of the CQRS split.
-- They are maintained by LeaseProjectionHandler reacting to domain events:
--   LeaseActivatedEvent  → upsert tenant_occupied_unit + insert unit_rental_history row
--   LeaseTerminatedEvent → delete tenant_occupied_unit + update unit_rental_history status
--   LeaseExpiredEvent    → delete tenant_occupied_unit + update unit_rental_history status

-- "Which unit is tenant X occupying right now?"  → O(1) primary-key lookup
CREATE TABLE IF NOT EXISTS tenant_occupied_unit (
    tenant_id     uuid PRIMARY KEY,
    unit_id       uuid,
    property_id   uuid,
    lease_id      uuid,
    monthly_rent  decimal,
    lease_start   date,
    lease_end     date,
    occupied_since timestamp
);

-- "Who has ever rented unit Y, newest first?"  → single-partition scan, no ALLOW FILTERING
CREATE TABLE IF NOT EXISTS unit_rental_history (
    unit_id      uuid,
    lease_start  date,
    lease_id     uuid,
    tenant_id    uuid,
    property_id  uuid,
    monthly_rent decimal,
    lease_end    date,
    status       text,   -- ACTIVE | TERMINATED | EXPIRED
    PRIMARY KEY ((unit_id), lease_start, lease_id)
) WITH CLUSTERING ORDER BY (lease_start DESC, lease_id ASC);

-- ── UNIT UNAVAILABILITY (auto-created by Spring on startup) ───────────────────

CREATE TABLE IF NOT EXISTS unit_unavailability (
    id         uuid PRIMARY KEY,
    unit_id    uuid,
    reason     text,
    start_date date,
    end_date   date,       -- nullable = open-ended
    created_at timestamp
);

CREATE INDEX IF NOT EXISTS idx_unavailability_unit ON unit_unavailability (unit_id);

-- ── USER Bounded Context ──────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id           uuid PRIMARY KEY,
    username     text,
    email        text,
    password_hash text,
    role         text,
    active       boolean,
    created_at   timestamp
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);

-- ── MPESA TRANSACTIONS (Billing Bounded Context) ─────────────────────────────

CREATE TABLE IF NOT EXISTS mpesa_transactions (
    checkout_request_id    text PRIMARY KEY,
    merchant_request_id    text,
    payment_transaction_id uuid,
    tenant_id              uuid,
    phone_number           text,
    amount                 decimal,
    status                 text,
    mpesa_receipt_number   text,
    initiated_at           timestamp,
    completed_at           timestamp
);

CREATE INDEX IF NOT EXISTS idx_mpesa_tx_payment ON mpesa_transactions (payment_transaction_id);

-- ── SETTINGS Bounded Context ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_settings (
    user_id    uuid PRIMARY KEY,
    currency   text,
    theme      text,
    timezone   text,
    updated_at timestamp
);
