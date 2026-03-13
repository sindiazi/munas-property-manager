package com.example.rentalmanager.maintenance.application.dto.command;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Command to transition a maintenance request to a new status. */
public record UpdateMaintenanceStatusCommand(
        @NotNull UUID requestId,
        @NotNull MaintenanceStatus newStatus,
        String resolutionNotes
) {}
