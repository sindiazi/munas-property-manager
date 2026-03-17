package com.example.rentalmanager.maintenance.application.dto.response;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;

public record MaintenanceIssueTemplateResponse(
        String id,
        String title,
        String description,
        MaintenancePriority priority
) {}
