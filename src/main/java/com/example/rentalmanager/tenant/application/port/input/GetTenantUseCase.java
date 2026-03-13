package com.example.rentalmanager.tenant.application.port.input;

import com.example.rentalmanager.tenant.application.dto.response.TenantResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Input port for querying tenants. */
public interface GetTenantUseCase {
    Mono<TenantResponse> getById(UUID tenantId);
    Flux<TenantResponse> getAll();
}
