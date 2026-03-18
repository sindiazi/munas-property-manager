package com.example.rentalmanager.payment.application.port.output;

import com.example.rentalmanager.payment.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.payment.domain.valueobject.MpesaTransactionStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Secondary port for persisting and querying M-Pesa transaction records. */
public interface MpesaTransactionPort {
    Mono<MpesaTransaction> save(MpesaTransaction tx);
    Mono<MpesaTransaction> findByCheckoutRequestId(String checkoutRequestId);
    Mono<MpesaTransaction> findByPaymentId(UUID paymentId);
    Mono<MpesaTransaction> updateStatus(String checkoutRequestId, MpesaTransactionStatus status,
                                        String receiptNumber);
}
