package com.example.rentalmanager.leasing.application.port.input;

import com.example.rentalmanager.leasing.application.dto.response.LeaseResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Input port for activating a DRAFT lease. */
public interface ActivateLeaseUseCase {
    Mono<LeaseResponse> activateLease(UUID leaseId);
}
