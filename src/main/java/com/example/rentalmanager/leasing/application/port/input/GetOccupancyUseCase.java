package com.example.rentalmanager.leasing.application.port.input;

import com.example.rentalmanager.leasing.application.dto.response.TenantOccupancyResponse;
import com.example.rentalmanager.leasing.application.dto.response.UnitRentalHistoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Query-side use cases backed by CQRS read-model projections. */
public interface GetOccupancyUseCase {

    /** Returns the unit a tenant currently occupies, or empty if they hold no active lease. */
    Mono<TenantOccupancyResponse> getCurrentOccupancy(UUID tenantId);

    /** Returns the full rental history for a unit, newest lease first. */
    Flux<UnitRentalHistoryResponse> getUnitHistory(UUID unitId);
}
