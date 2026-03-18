package com.example.rentalmanager.billing.application.port.output;

import com.example.rentalmanager.billing.domain.aggregate.Invoice;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceId;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface InvoicePersistencePort {
    Mono<Invoice> save(Invoice invoice);
    Flux<Invoice> findAll();
    Mono<Invoice> findById(InvoiceId id);
    Flux<Invoice> findByLeaseId(UUID leaseId);
    Flux<Invoice> findByTenantId(UUID tenantId);
    Flux<Invoice> findByStatus(InvoiceStatus status);
    Flux<Invoice> findByLeaseIdAndDueDateBetween(UUID leaseId, LocalDate from, LocalDate to);
}
