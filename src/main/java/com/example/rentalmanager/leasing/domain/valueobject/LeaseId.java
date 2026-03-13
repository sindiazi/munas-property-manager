package com.example.rentalmanager.leasing.domain.valueobject;

import java.util.UUID;

/** Strongly-typed identity for the {@code Lease} aggregate root. */
public record LeaseId(UUID value) {

    public LeaseId { if (value == null) throw new IllegalArgumentException("LeaseId must not be null"); }

    public static LeaseId generate()        { return new LeaseId(UUID.randomUUID()); }
    public static LeaseId of(UUID value)    { return new LeaseId(value); }
    public static LeaseId of(String value)  { return new LeaseId(UUID.fromString(value)); }

    @Override public String toString()      { return value.toString(); }
}
