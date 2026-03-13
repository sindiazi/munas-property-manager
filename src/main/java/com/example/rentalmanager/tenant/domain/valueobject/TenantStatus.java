package com.example.rentalmanager.tenant.domain.valueobject;

/** Lifecycle status of a tenant account. */
public enum TenantStatus {
    /** Account is active; tenant can sign leases. */
    ACTIVE,
    /** Account is inactive (e.g. no current lease). */
    INACTIVE,
    /** Tenant is blacklisted; not allowed to sign new leases. */
    BLACKLISTED
}
