package com.example.rentalmanager.tenant.application.port.input;

import com.example.rentalmanager.tenant.application.dto.response.TenantResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ActivateTenantUseCase {
    Mono<TenantResponse> activate(UUID tenantId);
}
