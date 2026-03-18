package com.example.rentalmanager.billing.application.port.input;

import com.example.rentalmanager.billing.application.dto.response.PaymentTransactionResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface GetPaymentsByInvoiceUseCase {
    Flux<PaymentTransactionResponse> getByInvoiceId(UUID invoiceId);
}
