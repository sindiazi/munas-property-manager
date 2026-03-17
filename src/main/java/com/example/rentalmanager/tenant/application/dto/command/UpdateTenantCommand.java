package com.example.rentalmanager.tenant.application.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/** Command to update mutable tenant fields. */
public record UpdateTenantCommand(
        UUID    tenantId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email    String email,
        @NotBlank String phoneNumber,
        @Min(300) @Max(850) int creditScore
) {}
