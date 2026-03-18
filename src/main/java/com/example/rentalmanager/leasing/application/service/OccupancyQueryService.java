package com.example.rentalmanager.leasing.application.service;

import com.example.rentalmanager.leasing.application.dto.response.TenantOccupancyResponse;
import com.example.rentalmanager.leasing.application.dto.response.UnitRentalHistoryResponse;
import com.example.rentalmanager.leasing.application.port.input.GetOccupancyUseCase;
import com.example.rentalmanager.leasing.application.port.output.OccupancyProjectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Query service reading from CQRS projection tables via {@link OccupancyProjectionPort}.
 * No domain objects are involved — these are pure read-model lookups.
 */
@Service
@RequiredArgsConstructor
public class OccupancyQueryService implements GetOccupancyUseCase {

    private final OccupancyProjectionPort projectionPort;

    @Override
    public Mono<TenantOccupancyResponse> getCurrentOccupancy(UUID tenantId) {
        return projectionPort.findCurrentOccupancy(tenantId);
    }

    @Override
    public Flux<UnitRentalHistoryResponse> getUnitHistory(UUID unitId) {
        return projectionPort.findUnitHistory(unitId);
    }
}
