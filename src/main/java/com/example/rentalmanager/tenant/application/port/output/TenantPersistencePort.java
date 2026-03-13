package com.example.rentalmanager.tenant.application.port.output;

import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Output port for tenant persistence operations. */
public interface TenantPersistencePort {
    Mono<Tenant>  save(Tenant tenant);
    Mono<Tenant>  findById(TenantId id);
    Mono<Tenant>  findByEmail(String email);
    Flux<Tenant>  findAll();
    Mono<Boolean> existsByEmail(String email);
}
