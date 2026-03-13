package com.example.rentalmanager.maintenance.infrastructure.persistence.adapter;

import com.example.rentalmanager.maintenance.application.port.output.MaintenancePersistencePort;
import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceRequest;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import com.example.rentalmanager.maintenance.domain.valueobject.RequestId;
import com.example.rentalmanager.maintenance.infrastructure.persistence.mapper.MaintenancePersistenceMapper;
import com.example.rentalmanager.maintenance.infrastructure.persistence.repository.MaintenanceRequestR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Secondary adapter implementing {@link MaintenancePersistencePort} with R2DBC. */
@Component
@RequiredArgsConstructor
public class MaintenancePersistenceAdapter implements MaintenancePersistencePort {

    private final MaintenanceRequestR2dbcRepository repository;
    private final MaintenancePersistenceMapper       mapper;

    @Override public Mono<MaintenanceRequest> save(MaintenanceRequest r)             { return repository.save(mapper.toEntity(r)).map(mapper::toDomain); }
    @Override public Mono<MaintenanceRequest> findById(RequestId id)                 { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Flux<MaintenanceRequest> findByPropertyId(UUID propertyId)      { return repository.findByPropertyId(propertyId).map(mapper::toDomain); }
    @Override public Flux<MaintenanceRequest> findByTenantId(UUID tenantId)          { return repository.findByTenantId(tenantId).map(mapper::toDomain); }
    @Override public Flux<MaintenanceRequest> findByStatus(MaintenanceStatus status) { return repository.findByStatus(status).map(mapper::toDomain); }
}
