package com.example.rentalmanager.maintenance.application.dto.response;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;

import java.time.Instant;
import java.util.UUID;

/** Read-model DTO for a maintenance request. */
public record MaintenanceRequestResponse(
        UUID id,
        UUID propertyId,
        UUID unitId,
        UUID tenantId,
        String problemDescription,
        String resolutionNotes,
        MaintenancePriority priority,
        MaintenanceStatus status,
        Instant requestedAt,
        Instant completedAt
) {}
