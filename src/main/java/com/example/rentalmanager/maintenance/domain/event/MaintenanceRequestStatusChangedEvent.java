package com.example.rentalmanager.maintenance.domain.event;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import com.example.rentalmanager.maintenance.domain.valueobject.RequestId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised whenever a maintenance request transitions to a new status. */
public record MaintenanceRequestStatusChangedEvent(
        UUID eventId,
        Instant occurredOn,
        RequestId requestId,
        MaintenanceStatus previousStatus,
        MaintenanceStatus newStatus
) implements DomainEvent {}
