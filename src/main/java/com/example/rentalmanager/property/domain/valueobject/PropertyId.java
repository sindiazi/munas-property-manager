package com.example.rentalmanager.property.domain.valueobject;

import java.util.UUID;

/**
 * Strongly-typed identity for the {@code Property} aggregate root.
 * Using a dedicated record prevents accidental confusion between
 * different UUID-based identifiers.
 */
public record PropertyId(UUID value) {

    public PropertyId {
        if (value == null) throw new IllegalArgumentException("PropertyId value must not be null");
    }

    public static PropertyId generate() {
        return new PropertyId(UUID.randomUUID());
    }

    public static PropertyId of(UUID value) {
        return new PropertyId(value);
    }

    public static PropertyId of(String value) {
        return new PropertyId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
