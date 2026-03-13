package com.example.rentalmanager.property.domain.event;

import com.example.rentalmanager.property.domain.valueobject.PropertyId;
import com.example.rentalmanager.property.domain.valueobject.UnitId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a new {@code PropertyUnit} is added to a {@code Property}. */
public record PropertyUnitAddedEvent(
        UUID eventId,
        Instant occurredOn,
        PropertyId propertyId,
        UnitId unitId
) implements DomainEvent {}
