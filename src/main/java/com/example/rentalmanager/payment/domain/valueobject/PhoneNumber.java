package com.example.rentalmanager.payment.domain.valueobject;

/** Strongly-typed phone number normalised to the 2547XXXXXXXX format. */
public record PhoneNumber(String value) {

    public PhoneNumber {
        if (!value.matches("2547\\d{8}"))
            throw new IllegalArgumentException("Phone must be 2547XXXXXXXX, got: " + value);
    }

    /** Normalises 07XX, 01XX, +2547XX → 2547XXXXXXXX. */
    public static PhoneNumber of(String raw) {
        String s = raw.replaceAll("\\s+", "");
        if (s.startsWith("+")) s = s.substring(1);
        if (s.startsWith("07") || s.startsWith("01")) s = "254" + s.substring(1);
        return new PhoneNumber(s);
    }
}
