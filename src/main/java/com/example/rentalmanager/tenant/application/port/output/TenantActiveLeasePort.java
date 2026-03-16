package com.example.rentalmanager.tenant.application.port.output;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Secondary port: checks whether a tenant currently has an active lease.
 * Implemented by querying the {@code tenant_occupied_unit} CQRS projection.
 */
public interface TenantActiveLeasePort {

    /** Returns {@code true} if the tenant has an active lease (i.e. a row exists in the projection). */
    Mono<Boolean> hasActiveLease(UUID tenantId);
}
