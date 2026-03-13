package com.example.rentalmanager.leasing.domain.event;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a new lease is created in DRAFT status. */
public record LeaseCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        LeaseId leaseId,
        UUID tenantId,
        UUID propertyId,
        UUID unitId
) implements DomainEvent {}
