package com.example.rentalmanager.billing.application.port.input;

import com.example.rentalmanager.billing.application.dto.response.MpesaStatusResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface QueryMpesaStatusUseCase {
    Mono<MpesaStatusResponse> queryStatus(UUID paymentTransactionId);
}
