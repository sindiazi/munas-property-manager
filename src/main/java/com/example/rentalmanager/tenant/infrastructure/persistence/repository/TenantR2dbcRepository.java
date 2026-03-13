package com.example.rentalmanager.tenant.infrastructure.persistence.repository;

import com.example.rentalmanager.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Spring Data R2DBC repository for {@link TenantJpaEntity}. */
public interface TenantR2dbcRepository extends ReactiveCrudRepository<TenantJpaEntity, UUID> {

    Mono<TenantJpaEntity> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
}
