package com.example.rentalmanager.property.infrastructure.web.controller;

import com.example.rentalmanager.property.application.dto.command.MarkUnitUnavailableCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyUnitResponse;
import com.example.rentalmanager.property.application.dto.response.UnitUnavailabilityResponse;
import com.example.rentalmanager.property.application.port.input.GetUnitUnavailabilityUseCase;
import com.example.rentalmanager.property.application.port.input.MarkUnitAvailableUseCase;
import com.example.rentalmanager.property.application.port.input.MarkUnitUnavailableUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Primary adapter exposing unit availability endpoints over HTTP / WebFlux. */
@Tag(name = "Units", description = "Property unit availability management")
@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitController {

    private final MarkUnitUnavailableUseCase   markUnavailableUseCase;
    private final MarkUnitAvailableUseCase     markAvailableUseCase;
    private final GetUnitUnavailabilityUseCase getUnavailabilityUseCase;

    @Operation(summary = "Mark a unit unavailable (ADMIN / PROPERTY_MANAGER)")
    @PatchMapping("/{unitId}/unavailable")
    public Mono<PropertyUnitResponse> markUnavailable(
            @PathVariable UUID unitId,
            @Valid @RequestBody MarkUnitUnavailableCommand body) {
        var cmd = new MarkUnitUnavailableCommand(unitId, body.reason(), body.startDate(), body.endDate());
        return markUnavailableUseCase.markUnavailable(cmd);
    }

    @Operation(summary = "Mark a unit available again (ADMIN / PROPERTY_MANAGER)")
    @PatchMapping("/{unitId}/available")
    public Mono<PropertyUnitResponse> markAvailable(@PathVariable UUID unitId) {
        return markAvailableUseCase.markAvailable(unitId);
    }

    @Operation(summary = "Get unavailability history for a unit")
    @GetMapping("/{unitId}/unavailability")
    public Flux<UnitUnavailabilityResponse> getHistory(@PathVariable UUID unitId) {
        return getUnavailabilityUseCase.getHistory(unitId);
    }
}
