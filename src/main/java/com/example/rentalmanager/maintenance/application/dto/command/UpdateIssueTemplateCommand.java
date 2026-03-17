package com.example.rentalmanager.maintenance.application.dto.command;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;

/** All fields except {@code categoryId} and {@code id} are optional — null means "keep existing". */
public record UpdateIssueTemplateCommand(
        String categoryId,
        String id,
        String title,
        String description,
        MaintenancePriority priority
) {}
