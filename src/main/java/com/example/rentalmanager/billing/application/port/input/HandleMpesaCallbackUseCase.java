package com.example.rentalmanager.billing.application.port.input;

import com.example.rentalmanager.billing.application.dto.command.MpesaCallbackCommand;
import reactor.core.publisher.Mono;

public interface HandleMpesaCallbackUseCase {
    Mono<Void> handleCallback(MpesaCallbackCommand command);
}
