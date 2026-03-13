package com.example.rentalmanager.payment.application.port.input;

import com.example.rentalmanager.payment.application.dto.command.ProcessPaymentCommand;
import com.example.rentalmanager.payment.application.dto.response.PaymentResponse;
import reactor.core.publisher.Mono;

public interface ProcessPaymentUseCase {
    Mono<PaymentResponse> processPayment(ProcessPaymentCommand command);
}
