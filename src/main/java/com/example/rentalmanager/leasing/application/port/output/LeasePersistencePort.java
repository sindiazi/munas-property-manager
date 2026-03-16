package com.example.rentalmanager.leasing.application.port.output;

import com.example.rentalmanager.leasing.domain.aggregate.Lease;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Output port for lease persistence operations. */
public interface LeasePersistencePort {
    Mono<Lease>  save(Lease lease);
    Flux<Lease>  findAll();
    Mono<Lease>  findById(LeaseId id);
    Flux<Lease>  findByTenantId(UUID tenantId);
    Flux<Lease>  findByUnitId(UUID unitId);
    Flux<Lease>  findByStatus(LeaseStatus status);
    /**
     * Returns any lease in a non-terminal state (DRAFT or ACTIVE) for the given unit,
     * or empty if the unit is free to be leased.
     */
    Mono<Lease>  findNonTerminalLeaseByUnitId(UUID unitId);
}
