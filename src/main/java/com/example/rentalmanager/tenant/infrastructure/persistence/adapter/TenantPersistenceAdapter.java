package com.example.rentalmanager.tenant.infrastructure.persistence.adapter;

import com.example.rentalmanager.tenant.application.port.output.TenantPersistencePort;
import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import com.example.rentalmanager.tenant.infrastructure.persistence.mapper.TenantPersistenceMapper;
import com.example.rentalmanager.tenant.infrastructure.persistence.repository.TenantR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Secondary adapter — implements {@link TenantPersistencePort} using Spring Data R2DBC. */
@Component
@RequiredArgsConstructor
public class TenantPersistenceAdapter implements TenantPersistencePort {

    private final TenantR2dbcRepository   repository;
    private final TenantPersistenceMapper mapper;

    @Override
    public Mono<Tenant> save(Tenant tenant) {
        return repository.save(mapper.toEntity(tenant)).map(mapper::toDomain);
    }

    @Override
    public Mono<Tenant> findById(TenantId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<Tenant> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Flux<Tenant> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
