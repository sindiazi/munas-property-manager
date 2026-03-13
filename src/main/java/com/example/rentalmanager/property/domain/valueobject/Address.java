package com.example.rentalmanager.property.domain.valueobject;

/**
 * Immutable Value Object representing a physical address.
 *
 * <p>Two addresses are equal when all their components are equal — no
 * identity-based equality is needed.
 */
public record Address(
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {
    public Address {
        assertNotBlank(street,  "street");
        assertNotBlank(city,    "city");
        assertNotBlank(state,   "state");
        assertNotBlank(zipCode, "zipCode");
        assertNotBlank(country, "country");
    }

    public String fullAddress() {
        return "%s, %s, %s %s, %s".formatted(street, city, state, zipCode, country);
    }

    private static void assertNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Address." + field + " must not be blank");
        }
    }
}
