package com.example.rentalmanager.billing.infrastructure.persistence.repository;

import com.example.rentalmanager.billing.infrastructure.persistence.entity.MpesaTransactionEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MpesaTransactionRepository extends ReactiveCassandraRepository<MpesaTransactionEntity, String> {

    Mono<MpesaTransactionEntity> findByPaymentTransactionId(UUID paymentTransactionId);
}
