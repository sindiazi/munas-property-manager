package com.example.rentalmanager.leasing.domain.event;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when a lease is terminated before its natural end date. */
public record LeaseTerminatedEvent(
        UUID eventId,
        Instant occurredOn,
        LeaseId leaseId,
        UUID tenantId,
        UUID unitId,
        String reason
) implements DomainEvent {}
