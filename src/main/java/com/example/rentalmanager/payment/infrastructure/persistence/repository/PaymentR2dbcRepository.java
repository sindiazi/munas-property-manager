package com.example.rentalmanager.payment.infrastructure.persistence.repository;

import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import com.example.rentalmanager.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/** Spring Data R2DBC repository for {@link PaymentJpaEntity}. */
public interface PaymentR2dbcRepository extends ReactiveCrudRepository<PaymentJpaEntity, UUID> {

    Flux<PaymentJpaEntity> findByLeaseId(UUID leaseId);
    Flux<PaymentJpaEntity> findByTenantId(UUID tenantId);
    Flux<PaymentJpaEntity> findByStatus(PaymentStatus status);
}
