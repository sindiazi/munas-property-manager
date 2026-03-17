package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when an issue template within a maintenance category is updated. */
public record MaintenanceIssueTemplateUpdatedEvent(
        UUID eventId,
        Instant occurredOn,
        String categoryId,
        String issueId
) implements DomainEvent {}
