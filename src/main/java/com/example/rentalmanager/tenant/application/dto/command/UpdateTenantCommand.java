package com.example.rentalmanager.tenant.application.dto.command;

import jakarta.validation.constraints.Email;

import java.util.UUID;

/** Command to update mutable tenant fields. */
public record UpdateTenantCommand(
        UUID tenantId,
        @Email String email,
        String phoneNumber
) {}
