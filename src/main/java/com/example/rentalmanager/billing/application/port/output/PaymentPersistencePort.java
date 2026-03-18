package com.example.rentalmanager.billing.application.port.output;

import com.example.rentalmanager.billing.domain.aggregate.Payment;
import com.example.rentalmanager.billing.domain.valueobject.PaymentId;
import com.example.rentalmanager.billing.domain.valueobject.PaymentTransactionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentPersistencePort {
    Mono<Payment> save(Payment payment);
    Mono<Payment> findById(PaymentId id);
    Flux<Payment> findByInvoiceId(UUID invoiceId);
    Mono<Payment> updateStatus(UUID paymentId, PaymentTransactionStatus status, String reference);
}
