package com.example.rentalmanager.maintenance.application.port.output;

import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceRequest;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import com.example.rentalmanager.maintenance.domain.valueobject.RequestId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MaintenancePersistencePort {
    Mono<MaintenanceRequest> save(MaintenanceRequest request);
    Mono<MaintenanceRequest> findById(RequestId id);
    Flux<MaintenanceRequest> findByPropertyId(UUID propertyId);
    Flux<MaintenanceRequest> findByTenantId(UUID tenantId);
    Flux<MaintenanceRequest> findByStatus(MaintenanceStatus status);
}
