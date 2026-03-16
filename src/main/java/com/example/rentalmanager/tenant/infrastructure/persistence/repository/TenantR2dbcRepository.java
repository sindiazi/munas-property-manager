package com.example.rentalmanager.tenant.infrastructure.persistence.repository;

import com.example.rentalmanager.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Spring Data Cassandra repository for {@link TenantJpaEntity}. */
public interface TenantR2dbcRepository extends ReactiveCassandraRepository<TenantJpaEntity, UUID> {

    Mono<TenantJpaEntity> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Mono<TenantJpaEntity> findByNationalIdNoHash(String nationalIdNoHash);
}
