package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a maintenance category (and all its issue templates) is deleted. */
public record MaintenanceCategoryDeletedEvent(
        UUID eventId,
        Instant occurredOn,
        String categoryId
) implements DomainEvent {}
