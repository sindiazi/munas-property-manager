package com.example.rentalmanager.billing.infrastructure.persistence.repository;

import com.example.rentalmanager.billing.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PaymentCassandraRepository extends ReactiveCassandraRepository<PaymentEntity, UUID> {

    Flux<PaymentEntity> findByInvoiceId(UUID invoiceId);
}
