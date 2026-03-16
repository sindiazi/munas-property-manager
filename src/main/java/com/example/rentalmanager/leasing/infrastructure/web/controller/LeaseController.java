package com.example.rentalmanager.leasing.infrastructure.web.controller;

import com.example.rentalmanager.leasing.application.dto.command.CreateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.command.TerminateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.response.LeaseResponse;
import com.example.rentalmanager.leasing.application.port.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Primary adapter exposing Leasing operations over HTTP / WebFlux. */
@Tag(name = "Leases", description = "Lease agreement lifecycle management")
@RestController
@RequestMapping("/api/v1/leases")
@RequiredArgsConstructor
public class LeaseController {

    private final CreateLeaseUseCase    createLeaseUseCase;
    private final ActivateLeaseUseCase  activateLeaseUseCase;
    private final TerminateLeaseUseCase terminateLeaseUseCase;
    private final GetLeaseUseCase       getLeaseUseCase;

    @Operation(summary = "Draft a new lease agreement")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LeaseResponse> createLease(@Valid @RequestBody CreateLeaseCommand command) {
        return createLeaseUseCase.createLease(command);
    }

    @Operation(summary = "Activate a draft lease")
    @PatchMapping("/{id}/activate")
    public Mono<LeaseResponse> activate(@PathVariable UUID id) {
        return activateLeaseUseCase.activateLease(id);
    }

    @Operation(summary = "Terminate an active lease")
    @PatchMapping("/{id}/terminate")
    public Mono<LeaseResponse> terminate(
            @PathVariable UUID id,
            @Valid @RequestBody TerminateLeaseCommand command) {
        var merged = new TerminateLeaseCommand(id, command.reason());
        return terminateLeaseUseCase.terminateLease(merged);
    }

    @Operation(summary = "List all leases")
    @GetMapping
    public Flux<LeaseResponse> getAll() {
        return getLeaseUseCase.getAll();
    }

    @Operation(summary = "Get lease by ID")
    @GetMapping("/{id}")
    public Mono<LeaseResponse> getById(@PathVariable UUID id) {
        return getLeaseUseCase.getById(id);
    }

    @Operation(summary = "Get leases by tenant")
    @GetMapping("/tenant/{tenantId}")
    public Flux<LeaseResponse> getByTenant(@PathVariable UUID tenantId) {
        return getLeaseUseCase.getByTenantId(tenantId);
    }
}
