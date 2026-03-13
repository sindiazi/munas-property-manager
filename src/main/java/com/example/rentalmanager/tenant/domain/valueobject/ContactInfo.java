package com.example.rentalmanager.tenant.domain.valueobject;

/**
 * Value Object holding a tenant's contact information.
 */
public record ContactInfo(
        String email,
        String phoneNumber
) {
    public ContactInfo {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("Valid email required");
        if (phoneNumber == null || phoneNumber.isBlank()) throw new IllegalArgumentException("Phone required");
    }
}
