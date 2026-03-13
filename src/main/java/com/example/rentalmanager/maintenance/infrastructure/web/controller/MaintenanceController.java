package com.example.rentalmanager.maintenance.infrastructure.web.controller;

import com.example.rentalmanager.maintenance.application.dto.command.CreateMaintenanceRequestCommand;
import com.example.rentalmanager.maintenance.application.dto.command.UpdateMaintenanceStatusCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceRequestResponse;
import com.example.rentalmanager.maintenance.application.port.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Primary adapter exposing Maintenance operations over HTTP / WebFlux. */
@Tag(name = "Maintenance", description = "Maintenance request lifecycle management")
@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final CreateMaintenanceRequestUseCase createUseCase;
    private final UpdateMaintenanceRequestUseCase updateUseCase;
    private final GetMaintenanceRequestUseCase    getUseCase;

    @Operation(summary = "Open a new maintenance request")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MaintenanceRequestResponse> create(@Valid @RequestBody CreateMaintenanceRequestCommand cmd) {
        return createUseCase.create(cmd);
    }

    @Operation(summary = "Update the status of a maintenance request")
    @PatchMapping("/{id}/status")
    public Mono<MaintenanceRequestResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaintenanceStatusCommand cmd) {
        var merged = new UpdateMaintenanceStatusCommand(id, cmd.newStatus(), cmd.resolutionNotes());
        return updateUseCase.updateStatus(merged);
    }

    @Operation(summary = "Get maintenance request by ID")
    @GetMapping("/{id}")
    public Mono<MaintenanceRequestResponse> getById(@PathVariable UUID id) {
        return getUseCase.getById(id);
    }

    @Operation(summary = "Get maintenance requests for a property")
    @GetMapping("/property/{propertyId}")
    public Flux<MaintenanceRequestResponse> getByProperty(@PathVariable UUID propertyId) {
        return getUseCase.getByPropertyId(propertyId);
    }

    @Operation(summary = "Get maintenance requests submitted by a tenant")
    @GetMapping("/tenant/{tenantId}")
    public Flux<MaintenanceRequestResponse> getByTenant(@PathVariable UUID tenantId) {
        return getUseCase.getByTenantId(tenantId);
    }
}
