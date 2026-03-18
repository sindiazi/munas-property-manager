package com.example.rentalmanager.leasing.application.port.output;

import com.example.rentalmanager.leasing.application.dto.response.TenantOccupancyResponse;
import com.example.rentalmanager.leasing.application.dto.response.UnitRentalHistoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Secondary port for reading CQRS projection tables.
 * Returns response DTOs directly — no domain objects involved in read-model queries.
 */
public interface OccupancyProjectionPort {
    Mono<TenantOccupancyResponse>    findCurrentOccupancy(UUID tenantId);
    Flux<UnitRentalHistoryResponse>  findUnitHistory(UUID unitId);
}
