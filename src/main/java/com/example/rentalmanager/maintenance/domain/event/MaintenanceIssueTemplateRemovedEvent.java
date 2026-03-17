package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when an issue template is removed from a maintenance category. */
public record MaintenanceIssueTemplateRemovedEvent(
        UUID eventId,
        Instant occurredOn,
        String categoryId,
        String issueId
) implements DomainEvent {}
