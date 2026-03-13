package com.example.rentalmanager.payment.domain.valueobject;

import java.util.UUID;

/** Strongly-typed identity for the {@code Payment} aggregate root. */
public record PaymentId(UUID value) {

    public PaymentId { if (value == null) throw new IllegalArgumentException("PaymentId must not be null"); }

    public static PaymentId generate()       { return new PaymentId(UUID.randomUUID()); }
    public static PaymentId of(UUID value)   { return new PaymentId(value); }
    public static PaymentId of(String value) { return new PaymentId(UUID.fromString(value)); }

    @Override public String toString()       { return value.toString(); }
}
