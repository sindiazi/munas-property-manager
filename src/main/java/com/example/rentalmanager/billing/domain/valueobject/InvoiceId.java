package com.example.rentalmanager.billing.domain.valueobject;

import java.util.UUID;

public record InvoiceId(UUID value) {

    public InvoiceId { if (value == null) throw new IllegalArgumentException("InvoiceId must not be null"); }

    public static InvoiceId generate()       { return new InvoiceId(UUID.randomUUID()); }
    public static InvoiceId of(UUID value)   { return new InvoiceId(value); }
    public static InvoiceId of(String value) { return new InvoiceId(UUID.fromString(value)); }

    @Override public String toString()       { return value.toString(); }
}
