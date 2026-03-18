package com.example.rentalmanager.shared.domain;

/**
 * Domain port for SSN masking and lookup-hash operations.
 * Implemented by {@code SsnEncryptionService} in the infrastructure layer.
 */
public interface SsnEncryptionPort {

    /** Returns a masked representation of the encrypted SSN (e.g. {@code "***-**-1234"}). */
    String mask(String plainSsn);

    /** Returns a deterministic HMAC hash of the plain SSN for equality lookups. */
    String computeLookupHash(String plainSsn);
}
