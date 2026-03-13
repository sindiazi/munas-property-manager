package com.example.rentalmanager.payment.application.port.input;

import com.example.rentalmanager.payment.application.dto.command.CreatePaymentCommand;
import com.example.rentalmanager.payment.application.dto.response.PaymentResponse;
import reactor.core.publisher.Mono;

public interface CreatePaymentUseCase {
    Mono<PaymentResponse> createPayment(CreatePaymentCommand command);
}
