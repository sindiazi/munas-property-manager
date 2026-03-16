package com.example.rentalmanager.leasing.infrastructure.web.controller;

import com.example.rentalmanager.leasing.application.dto.response.TenantOccupancyResponse;
import com.example.rentalmanager.leasing.application.dto.response.UnitRentalHistoryResponse;
import com.example.rentalmanager.leasing.application.port.input.GetOccupancyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Read-model (CQRS query-side) endpoints backed by projection tables.
 * Responses are served from denormalised Cassandra tables — no joins, O(1) / single-partition reads.
 */
@Tag(name = "Occupancy", description = "CQRS read-model: tenant occupancy and unit rental history")
@RestController
@RequestMapping("/api/v1/occupancy")
@RequiredArgsConstructor
public class OccupancyController {

    private final GetOccupancyUseCase occupancyUseCase;

    @Operation(
            summary = "Get a tenant's current unit",
            description = "Returns the unit a tenant is currently occupying based on their active lease. " +
                          "Returns 404 if the tenant has no active lease."
    )
    @GetMapping("/tenant/{tenantId}")
    public Mono<TenantOccupancyResponse> getCurrentOccupancy(@PathVariable UUID tenantId) {
        return occupancyUseCase.getCurrentOccupancy(tenantId);
    }

    @Operation(
            summary = "Get a unit's full rental history",
            description = "Returns all lease entries for a unit, newest first. " +
                          "Backed by the unit_rental_history projection table — single-partition read."
    )
    @GetMapping("/unit/{unitId}/history")
    public Flux<UnitRentalHistoryResponse> getUnitHistory(@PathVariable UUID unitId) {
        return occupancyUseCase.getUnitHistory(unitId);
    }
}
