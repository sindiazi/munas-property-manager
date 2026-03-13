package com.example.rentalmanager.leasing.infrastructure.persistence.repository;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.LeaseJpaEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Spring Data R2DBC repository for {@link LeaseJpaEntity}. */
public interface LeaseR2dbcRepository extends ReactiveCrudRepository<LeaseJpaEntity, UUID> {

    Flux<LeaseJpaEntity> findByTenantId(UUID tenantId);

    Flux<LeaseJpaEntity> findByUnitId(UUID unitId);

    Flux<LeaseJpaEntity> findByStatus(LeaseStatus status);

    Mono<LeaseJpaEntity> findByUnitIdAndStatus(UUID unitId, LeaseStatus status);
}
