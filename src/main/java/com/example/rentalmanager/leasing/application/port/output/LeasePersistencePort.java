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
    Mono<Lease>  findById(LeaseId id);
    Flux<Lease>  findByTenantId(UUID tenantId);
    Flux<Lease>  findByUnitId(UUID unitId);
    Flux<Lease>  findByStatus(LeaseStatus status);
    Mono<Lease>  findActiveLeaseByUnitId(UUID unitId);
}
