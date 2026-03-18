package com.example.rentalmanager.payment.application.port.input;

import com.example.rentalmanager.payment.application.dto.response.MpesaStatusResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface QueryMpesaStatusUseCase {
    Mono<MpesaStatusResponse> queryStatus(UUID paymentId);
}
