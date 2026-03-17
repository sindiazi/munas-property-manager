package com.example.rentalmanager.maintenance.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record CreateMaintenanceCategoryCommand(
        @NotBlank String id,
        @NotBlank String name
) {}
