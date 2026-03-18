package com.example.rentalmanager.billing.infrastructure.persistence.adapter;

import com.example.rentalmanager.billing.application.port.output.InvoicePersistencePort;
import com.example.rentalmanager.billing.domain.aggregate.Invoice;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceId;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.billing.infrastructure.persistence.mapper.InvoicePersistenceMapper;
import com.example.rentalmanager.billing.infrastructure.persistence.repository.InvoiceCassandraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InvoicePersistenceAdapter implements InvoicePersistencePort {

    private final InvoiceCassandraRepository repository;
    private final InvoicePersistenceMapper   mapper;

    @Override public Mono<Invoice> save(Invoice inv)                { return repository.save(mapper.toEntity(inv)).map(mapper::toDomain); }
    @Override public Flux<Invoice> findAll()                        { return repository.findAll().map(mapper::toDomain); }
    @Override public Mono<Invoice> findById(InvoiceId id)           { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Flux<Invoice> findByLeaseId(UUID leaseId)      { return repository.findByLeaseId(leaseId).map(mapper::toDomain); }
    @Override public Flux<Invoice> findByTenantId(UUID tenantId)    { return repository.findByTenantId(tenantId).map(mapper::toDomain); }
    @Override public Flux<Invoice> findByStatus(InvoiceStatus s)    { return repository.findByStatus(s).map(mapper::toDomain); }
    @Override public Flux<Invoice> findByLeaseIdAndDueDateBetween(UUID leaseId, LocalDate from, LocalDate to) {
        return repository.findByLeaseIdAndDueDateBetween(leaseId, from, to).map(mapper::toDomain);
    }
}
