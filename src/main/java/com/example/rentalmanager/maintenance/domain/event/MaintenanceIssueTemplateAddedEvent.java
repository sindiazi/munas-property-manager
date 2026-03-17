package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a new issue template is added to a maintenance category. */
public record MaintenanceIssueTemplateAddedEvent(
        UUID eventId,
        Instant occurredOn,
        String categoryId,
        String issueId,
        String title,
        MaintenancePriority priority
) implements DomainEvent {}
