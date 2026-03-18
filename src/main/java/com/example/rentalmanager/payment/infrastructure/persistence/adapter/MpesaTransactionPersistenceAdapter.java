package com.example.rentalmanager.payment.infrastructure.persistence.adapter;

import com.example.rentalmanager.payment.application.port.output.MpesaTransactionPort;
import com.example.rentalmanager.payment.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.payment.domain.valueobject.MpesaTransactionStatus;
import com.example.rentalmanager.payment.infrastructure.persistence.mapper.MpesaTransactionMapper;
import com.example.rentalmanager.payment.infrastructure.persistence.repository.MpesaTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/** Secondary adapter implementing {@link MpesaTransactionPort} against Cassandra. */
@Component
@RequiredArgsConstructor
public class MpesaTransactionPersistenceAdapter implements MpesaTransactionPort {

    private final MpesaTransactionRepository repository;
    private final MpesaTransactionMapper     mapper;

    @Override
    public Mono<MpesaTransaction> save(MpesaTransaction tx) {
        return repository.save(mapper.toEntity(tx)).map(mapper::toDomain);
    }

    @Override
    public Mono<MpesaTransaction> findByCheckoutRequestId(String checkoutRequestId) {
        return repository.findById(checkoutRequestId).map(mapper::toDomain);
    }

    @Override
    public Mono<MpesaTransaction> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentId(paymentId).map(mapper::toDomain);
    }

    @Override
    public Mono<MpesaTransaction> updateStatus(String checkoutRequestId,
                                               MpesaTransactionStatus status,
                                               String receiptNumber) {
        return repository.findById(checkoutRequestId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "MpesaTransaction not found: " + checkoutRequestId)))
                .flatMap(entity -> {
                    entity.setStatus(status);
                    entity.setMpesaReceiptNumber(receiptNumber);
                    if (status == MpesaTransactionStatus.CONFIRMED
                            || status == MpesaTransactionStatus.FAILED
                            || status == MpesaTransactionStatus.CANCELLED) {
                        entity.setCompletedAt(Instant.now());
                    }
                    return repository.save(entity);
                })
                .map(mapper::toDomain);
    }
}
