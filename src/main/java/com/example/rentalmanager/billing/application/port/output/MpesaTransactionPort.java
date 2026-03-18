package com.example.rentalmanager.billing.application.port.output;

import com.example.rentalmanager.billing.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.billing.domain.valueobject.MpesaTransactionStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MpesaTransactionPort {
    Mono<MpesaTransaction> save(MpesaTransaction tx);
    Mono<MpesaTransaction> findByCheckoutRequestId(String checkoutRequestId);
    Mono<MpesaTransaction> findByPaymentTransactionId(UUID paymentTransactionId);
    Mono<MpesaTransaction> updateStatus(String checkoutRequestId, MpesaTransactionStatus status, String receiptNumber);
}
