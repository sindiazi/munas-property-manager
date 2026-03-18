package com.example.rentalmanager.payment.application.port.input;

import com.example.rentalmanager.payment.application.dto.command.InitiateMpesaPaymentCommand;
import com.example.rentalmanager.payment.application.dto.response.MpesaInitiationResponse;
import reactor.core.publisher.Mono;

public interface InitiateMpesaPaymentUseCase {
    Mono<MpesaInitiationResponse> initiateMpesaPayment(InitiateMpesaPaymentCommand command);
}
