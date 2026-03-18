package com.example.rentalmanager.payment.infrastructure.persistence.repository;

import com.example.rentalmanager.payment.infrastructure.persistence.entity.MpesaTransactionEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MpesaTransactionRepository
        extends ReactiveCassandraRepository<MpesaTransactionEntity, String> {

    Mono<MpesaTransactionEntity> findByPaymentId(UUID paymentId);
}
