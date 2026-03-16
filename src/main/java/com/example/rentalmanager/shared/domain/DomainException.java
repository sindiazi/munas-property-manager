package com.example.rentalmanager.shared.domain;

/**
 * Base class for all domain-level exceptions.
 *
 * <p>Domain exceptions represent invariant violations and are always mapped to
 * HTTP 4xx responses by the global exception handler.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
