package com.example.rentalmanager.billing.application.port.input;

import com.example.rentalmanager.billing.application.dto.command.InitiateMpesaPaymentCommand;
import com.example.rentalmanager.billing.application.dto.response.MpesaInitiationResponse;
import reactor.core.publisher.Mono;

public interface InitiateMpesaPaymentUseCase {
    Mono<MpesaInitiationResponse> initiateMpesaPayment(InitiateMpesaPaymentCommand command);
}
