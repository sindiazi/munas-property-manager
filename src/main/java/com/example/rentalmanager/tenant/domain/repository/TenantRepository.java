package com.example.rentalmanager.tenant.domain.repository;

import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Output port for {@code Tenant} aggregate persistence. */
public interface TenantRepository {

    Mono<Tenant> save(Tenant tenant);

    Mono<Tenant> findById(TenantId id);

    Mono<Tenant> findByEmail(String email);

    Flux<Tenant> findAll();

    Mono<Boolean> existsByEmail(String email);
}
