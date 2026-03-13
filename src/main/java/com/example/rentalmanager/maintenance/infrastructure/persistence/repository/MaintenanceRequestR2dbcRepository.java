package com.example.rentalmanager.maintenance.infrastructure.persistence.repository;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceRequestJpaEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/** Spring Data R2DBC repository for {@link MaintenanceRequestJpaEntity}. */
public interface MaintenanceRequestR2dbcRepository
        extends ReactiveCrudRepository<MaintenanceRequestJpaEntity, UUID> {

    Flux<MaintenanceRequestJpaEntity> findByPropertyId(UUID propertyId);
    Flux<MaintenanceRequestJpaEntity> findByTenantId(UUID tenantId);
    Flux<MaintenanceRequestJpaEntity> findByStatus(MaintenanceStatus status);
}
