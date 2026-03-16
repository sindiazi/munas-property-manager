package com.example.rentalmanager.payment.application.port.input;

import com.example.rentalmanager.payment.application.dto.response.PaymentResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetPaymentUseCase {
    Flux<PaymentResponse> getAll();
    Mono<PaymentResponse> getById(UUID paymentId);
    Flux<PaymentResponse> getByLeaseId(UUID leaseId);
    Flux<PaymentResponse> getByTenantId(UUID tenantId);
}
