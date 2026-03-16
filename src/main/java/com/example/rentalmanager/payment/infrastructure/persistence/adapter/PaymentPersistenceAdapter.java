package com.example.rentalmanager.payment.infrastructure.persistence.adapter;

import com.example.rentalmanager.payment.application.port.output.PaymentPersistencePort;
import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import com.example.rentalmanager.payment.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.example.rentalmanager.payment.infrastructure.persistence.repository.PaymentR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Secondary adapter implementing {@link PaymentPersistencePort} with R2DBC. */
@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentPersistencePort {

    private final PaymentR2dbcRepository   repository;
    private final PaymentPersistenceMapper mapper;

    @Override public Mono<Payment> save(Payment p)                 { return repository.save(mapper.toEntity(p)).map(mapper::toDomain); }
    @Override public Flux<Payment> findAll()                       { return repository.findAll().map(mapper::toDomain); }
    @Override public Mono<Payment> findById(PaymentId id)          { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Flux<Payment> findByLeaseId(UUID leaseId)     { return repository.findByLeaseId(leaseId).map(mapper::toDomain); }
    @Override public Flux<Payment> findByTenantId(UUID tenantId)   { return repository.findByTenantId(tenantId).map(mapper::toDomain); }
    @Override public Flux<Payment> findByStatus(PaymentStatus s)   { return repository.findByStatus(s).map(mapper::toDomain); }
}
