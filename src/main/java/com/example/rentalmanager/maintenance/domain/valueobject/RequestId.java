package com.example.rentalmanager.maintenance.domain.valueobject;

import java.util.UUID;

/** Strongly-typed identity for a {@code MaintenanceRequest} aggregate root. */
public record RequestId(UUID value) {

    public RequestId { if (value == null) throw new IllegalArgumentException("RequestId must not be null"); }

    public static RequestId generate()       { return new RequestId(UUID.randomUUID()); }
    public static RequestId of(UUID value)   { return new RequestId(value); }
    public static RequestId of(String value) { return new RequestId(UUID.fromString(value)); }

    @Override public String toString()       { return value.toString(); }
}
