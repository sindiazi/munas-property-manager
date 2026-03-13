package com.example.rentalmanager.shared.domain;

/**
 * Base class for all domain-level exceptions.
 *
 * <p>Domain exceptions represent invariant violations and are always mapped to
 * HTTP 4xx responses by the global exception handler.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
