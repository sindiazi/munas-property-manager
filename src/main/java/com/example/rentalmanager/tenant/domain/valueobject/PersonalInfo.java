package com.example.rentalmanager.tenant.domain.valueobject;

/**
 * Value Object holding a tenant's personal identification details.
 */
public record PersonalInfo(
        String firstName,
        String lastName,
        String nationalId
) {
    public PersonalInfo {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName required");
        if (lastName  == null || lastName.isBlank())  throw new IllegalArgumentException("lastName required");
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
