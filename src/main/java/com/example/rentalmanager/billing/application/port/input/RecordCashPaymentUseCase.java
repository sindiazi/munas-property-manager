package com.example.rentalmanager.billing.application.port.input;

import com.example.rentalmanager.billing.application.dto.command.RecordCashPaymentCommand;
import com.example.rentalmanager.billing.application.dto.response.InvoiceResponse;
import reactor.core.publisher.Mono;

public interface RecordCashPaymentUseCase {
    Mono<InvoiceResponse> recordCashPayment(RecordCashPaymentCommand command);
}
