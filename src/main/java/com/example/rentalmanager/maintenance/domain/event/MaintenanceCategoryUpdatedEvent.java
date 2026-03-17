package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a maintenance category's name is updated. */
public record MaintenanceCategoryUpdatedEvent(
        UUID eventId,
        Instant occurredOn,
        String categoryId,
        String newName
) implements DomainEvent {}
