package com.example.rentalmanager.maintenance.application.dto.response;

import java.util.List;

public record MaintenanceCategoryResponse(
        String id,
        String name,
        List<MaintenanceIssueTemplateResponse> issues
) {}
