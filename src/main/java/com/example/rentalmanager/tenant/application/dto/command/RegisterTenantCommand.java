package com.example.rentalmanager.tenant.application.dto.command;

import jakarta.validation.constraints.*;

/** Command to register a new tenant in the system. */
public record RegisterTenantCommand(

        @NotBlank String firstName,
        @NotBlank String lastName,
        String nationalId,

        @Email @NotBlank String email,
        @NotBlank String phoneNumber,

        @Min(300) @Max(850) int creditScore
) {}
