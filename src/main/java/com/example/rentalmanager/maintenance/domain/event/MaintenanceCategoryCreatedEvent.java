package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a new maintenance category is created. */
public record MaintenanceCategoryCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        String categoryId,
        String name
) implements DomainEvent {}
