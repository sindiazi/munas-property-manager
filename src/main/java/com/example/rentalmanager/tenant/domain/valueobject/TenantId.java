package com.example.rentalmanager.tenant.domain.valueobject;

import java.util.UUID;

/** Strongly-typed identity for the {@code Tenant} aggregate root. */
public record TenantId(UUID value) {

    public TenantId {
        if (value == null) throw new IllegalArgumentException("TenantId must not be null");
    }

    public static TenantId generate()              { return new TenantId(UUID.randomUUID()); }
    public static TenantId of(UUID value)          { return new TenantId(value); }
    public static TenantId of(String value)        { return new TenantId(UUID.fromString(value)); }

    @Override public String toString()             { return value.toString(); }
}
