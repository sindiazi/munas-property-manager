package com.example.rentalmanager.payment.application.port.output;

import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentPersistencePort {
    Mono<Payment> save(Payment payment);
    Flux<Payment> findAll();
    Mono<Payment> findById(PaymentId id);
    Flux<Payment> findByLeaseId(UUID leaseId);
    Flux<Payment> findByTenantId(UUID tenantId);
    Flux<Payment> findByStatus(PaymentStatus status);
}
