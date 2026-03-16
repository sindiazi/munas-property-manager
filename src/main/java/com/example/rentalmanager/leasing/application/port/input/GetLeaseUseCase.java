package com.example.rentalmanager.leasing.application.port.input;

import com.example.rentalmanager.leasing.application.dto.response.LeaseResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Input port for querying lease agreements. */
public interface GetLeaseUseCase {
    Flux<LeaseResponse> getAll();
    Mono<LeaseResponse> getById(UUID leaseId);
    Flux<LeaseResponse> getByTenantId(UUID tenantId);
}
