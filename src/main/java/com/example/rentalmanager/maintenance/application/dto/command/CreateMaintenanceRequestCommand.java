package com.example.rentalmanager.maintenance.application.dto.command;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Command to open a new maintenance request. */
public record CreateMaintenanceRequestCommand(
        @NotNull UUID propertyId,
        @NotNull UUID unitId,
        @NotNull UUID tenantId,
        @NotBlank String problemDescription,
        @NotNull MaintenancePriority priority
) {}
