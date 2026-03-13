package com.example.rentalmanager.property.domain.event;

import com.example.rentalmanager.property.domain.valueobject.OwnerId;
import com.example.rentalmanager.property.domain.valueobject.PropertyId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a new {@code Property} is created. */
public record PropertyCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        PropertyId propertyId,
        OwnerId ownerId,
        String propertyName
) implements DomainEvent {}
