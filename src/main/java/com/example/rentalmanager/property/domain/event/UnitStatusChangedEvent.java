package com.example.rentalmanager.property.domain.event;

import com.example.rentalmanager.property.domain.valueobject.PropertyId;
import com.example.rentalmanager.property.domain.valueobject.UnitId;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised whenever a {@code PropertyUnit}'s status transitions. */
public record UnitStatusChangedEvent(
        UUID eventId,
        Instant occurredOn,
        PropertyId propertyId,
        UnitId unitId,
        UnitStatus previousStatus,
        UnitStatus newStatus
) implements DomainEvent {}
