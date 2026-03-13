package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceRequestResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetMaintenanceRequestUseCase {
    Mono<MaintenanceRequestResponse> getById(UUID requestId);
    Flux<MaintenanceRequestResponse> getByPropertyId(UUID propertyId);
    Flux<MaintenanceRequestResponse> getByTenantId(UUID tenantId);
}
