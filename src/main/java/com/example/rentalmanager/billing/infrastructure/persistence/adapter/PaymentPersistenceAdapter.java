package com.example.rentalmanager.billing.infrastructure.persistence.adapter;

import com.example.rentalmanager.billing.application.port.output.PaymentPersistencePort;
import com.example.rentalmanager.billing.domain.aggregate.Payment;
import com.example.rentalmanager.billing.domain.valueobject.PaymentId;
import com.example.rentalmanager.billing.domain.valueobject.PaymentTransactionStatus;
import com.example.rentalmanager.billing.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.example.rentalmanager.billing.infrastructure.persistence.repository.PaymentCassandraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentPersistencePort {

    private final PaymentCassandraRepository repository;
    private final PaymentPersistenceMapper   mapper;

    @Override public Mono<Payment> save(Payment p)              { return repository.save(mapper.toEntity(p)).map(mapper::toDomain); }
    @Override public Mono<Payment> findById(PaymentId id)       { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Flux<Payment> findByInvoiceId(UUID invoiceId) { return repository.findByInvoiceId(invoiceId).map(mapper::toDomain); }

    @Override
    public Mono<Payment> updateStatus(UUID paymentId, PaymentTransactionStatus status, String reference) {
        return repository.findById(paymentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Payment not found: " + paymentId)))
                .flatMap(entity -> {
                    entity.setStatus(status);
                    entity.setReference(reference);
                    return repository.save(entity);
                })
                .map(mapper::toDomain);
    }
}
