package com.example.rentalmanager.billing.infrastructure.persistence.repository;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.billing.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.UUID;

public interface InvoiceCassandraRepository extends ReactiveCassandraRepository<InvoiceEntity, UUID> {

    Flux<InvoiceEntity> findByLeaseId(UUID leaseId);
    Flux<InvoiceEntity> findByTenantId(UUID tenantId);
    Flux<InvoiceEntity> findByStatus(InvoiceStatus status);
    Flux<InvoiceEntity> findByLeaseIdAndDueDateBetween(UUID leaseId, LocalDate from, LocalDate to);
}
