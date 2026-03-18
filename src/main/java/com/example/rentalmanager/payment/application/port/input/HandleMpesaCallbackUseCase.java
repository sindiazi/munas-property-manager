package com.example.rentalmanager.payment.application.port.input;

import com.example.rentalmanager.payment.application.dto.command.MpesaCallbackCommand;
import reactor.core.publisher.Mono;

public interface HandleMpesaCallbackUseCase {
    Mono<Void> handleCallback(MpesaCallbackCommand command);
}
