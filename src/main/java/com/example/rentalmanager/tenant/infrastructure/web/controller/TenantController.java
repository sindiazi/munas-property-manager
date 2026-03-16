package com.example.rentalmanager.tenant.infrastructure.web.controller;

import com.example.rentalmanager.tenant.application.dto.command.RegisterTenantCommand;
import com.example.rentalmanager.tenant.application.dto.response.TenantResponse;
import com.example.rentalmanager.tenant.application.port.input.ActivateTenantUseCase;
import com.example.rentalmanager.tenant.application.port.input.DeactivateTenantUseCase;
import com.example.rentalmanager.tenant.application.port.input.GetTenantUseCase;
import com.example.rentalmanager.tenant.application.port.input.RegisterTenantUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Primary adapter exposing Tenant operations over HTTP / WebFlux. */
@Tag(name = "Tenants", description = "Tenant registration and management")
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final RegisterTenantUseCase   registerTenantUseCase;
    private final GetTenantUseCase        getTenantUseCase;
    private final ActivateTenantUseCase   activateTenantUseCase;
    private final DeactivateTenantUseCase deactivateTenantUseCase;

    @Operation(summary = "Register a new tenant")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TenantResponse> register(@Valid @RequestBody RegisterTenantCommand command) {
        return registerTenantUseCase.register(command);
    }

    @Operation(summary = "Get tenant by ID")
    @GetMapping("/{id}")
    public Mono<TenantResponse> getById(@PathVariable UUID id) {
        return getTenantUseCase.getById(id);
    }

    @Operation(summary = "List all tenants")
    @GetMapping
    public Flux<TenantResponse> getAll() {
        return getTenantUseCase.getAll();
    }

    @Operation(summary = "Activate an inactive tenant")
    @PatchMapping("/{id}/activate")
    public Mono<TenantResponse> activate(@PathVariable UUID id) {
        return activateTenantUseCase.activate(id);
    }

    @Operation(summary = "Deactivate an active tenant",
               description = "Fails if the tenant currently has an active lease.")
    @PatchMapping("/{id}/deactivate")
    public Mono<TenantResponse> deactivate(@PathVariable UUID id) {
        return deactivateTenantUseCase.deactivate(id);
    }
}
