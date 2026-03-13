package com.example.rentalmanager.property.domain.valueobject;

import java.util.UUID;

/** Strongly-typed identity for a {@code PropertyUnit} entity. */
public record UnitId(UUID value) {

    public UnitId {
        if (value == null) throw new IllegalArgumentException("UnitId value must not be null");
    }

    public static UnitId generate() {
        return new UnitId(UUID.randomUUID());
    }

    public static UnitId of(UUID value) {
        return new UnitId(value);
    }

    public static UnitId of(String value) {
        return new UnitId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
