package com.example.rentalmanager.maintenance.domain.repository;

import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceRequest;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import com.example.rentalmanager.maintenance.domain.valueobject.RequestId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Output port for {@code MaintenanceRequest} aggregate persistence. */
public interface MaintenanceRequestRepository {

    Mono<MaintenanceRequest> save(MaintenanceRequest request);
    Mono<MaintenanceRequest> findById(RequestId id);
    Flux<MaintenanceRequest> findByPropertyId(UUID propertyId);
    Flux<MaintenanceRequest> findByUnitId(UUID unitId);
    Flux<MaintenanceRequest> findByStatus(MaintenanceStatus status);
    Flux<MaintenanceRequest> findByTenantId(UUID tenantId);
}
