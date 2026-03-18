package com.example.rentalmanager.billing.application.port.input;

import com.example.rentalmanager.billing.application.dto.response.InvoiceResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetInvoiceUseCase {
    Flux<InvoiceResponse> getAll();
    Mono<InvoiceResponse> getById(UUID invoiceId);
    Flux<InvoiceResponse> getByLeaseId(UUID leaseId);
    Flux<InvoiceResponse> getByTenantId(UUID tenantId);
}
