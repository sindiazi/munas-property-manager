package com.example.rentalmanager.property.domain.valueobject;

import java.util.UUID;

/** Identifies the owner of a {@code Property}. Cross-context reference by ID only. */
public record OwnerId(UUID value) {

    public OwnerId {
        if (value == null) throw new IllegalArgumentException("OwnerId value must not be null");
    }

    public static OwnerId of(UUID value) {
        return new OwnerId(value);
    }

    public static OwnerId of(String value) {
        return new OwnerId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
