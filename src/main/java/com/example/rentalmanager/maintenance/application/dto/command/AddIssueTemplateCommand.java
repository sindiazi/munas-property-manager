package com.example.rentalmanager.maintenance.application.dto.command;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddIssueTemplateCommand(
        String categoryId,
        @NotBlank String id,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull  MaintenancePriority priority
) {}
