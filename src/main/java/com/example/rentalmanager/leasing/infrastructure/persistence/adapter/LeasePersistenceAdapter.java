package com.example.rentalmanager.leasing.infrastructure.persistence.adapter;

import com.example.rentalmanager.leasing.application.port.output.LeasePersistencePort;
import com.example.rentalmanager.leasing.domain.aggregate.Lease;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
import com.example.rentalmanager.leasing.infrastructure.persistence.mapper.LeasePersistenceMapper;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.LeaseR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Secondary adapter implementing {@link LeasePersistencePort} with Spring Data R2DBC. */
@Component
@RequiredArgsConstructor
public class LeasePersistenceAdapter implements LeasePersistencePort {

    private final LeaseR2dbcRepository   repository;
    private final LeasePersistenceMapper mapper;

    @Override public Mono<Lease>  save(Lease lease)                   { return repository.save(mapper.toEntity(lease)).map(mapper::toDomain); }
    @Override public Flux<Lease>  findAll()                           { return repository.findAll().map(mapper::toDomain); }
    @Override public Mono<Lease>  findById(LeaseId id)                { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Flux<Lease>  findByTenantId(UUID tenantId)       { return repository.findByTenantId(tenantId).map(mapper::toDomain); }
    @Override public Flux<Lease>  findByUnitId(UUID unitId)           { return repository.findByUnitId(unitId).map(mapper::toDomain); }
    @Override public Flux<Lease>  findByStatus(LeaseStatus status)    { return repository.findByStatus(status).map(mapper::toDomain); }
    @Override public Mono<Lease>  findNonTerminalLeaseByUnitId(UUID unitId) {
        // Check ACTIVE first (most common), fall back to DRAFT
        return repository.findByUnitIdAndStatus(unitId, LeaseStatus.ACTIVE)
                .switchIfEmpty(repository.findByUnitIdAndStatus(unitId, LeaseStatus.DRAFT))
                .map(mapper::toDomain);
    }
}
