package com.example.rentalmanager.payment.domain.repository;

import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Output port for {@code Payment} aggregate persistence. */
public interface PaymentRepository {

    Mono<Payment> save(Payment payment);
    Mono<Payment> findById(PaymentId id);
    Flux<Payment> findByLeaseId(UUID leaseId);
    Flux<Payment> findByTenantId(UUID tenantId);
    Flux<Payment> findByStatus(PaymentStatus status);
}
