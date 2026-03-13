package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.domain.valueobject.RequestId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a new maintenance request is opened. */
public record MaintenanceRequestCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        RequestId requestId,
        UUID propertyId,
        UUID unitId,
        UUID tenantId,
        MaintenancePriority priority
) implements DomainEvent {}
