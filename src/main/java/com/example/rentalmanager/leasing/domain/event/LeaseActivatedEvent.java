package com.example.rentalmanager.leasing.domain.event;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Raised when a lease transitions from DRAFT to ACTIVE. */
public record LeaseActivatedEvent(
        UUID eventId,
        Instant occurredOn,
        LeaseId leaseId,
        UUID tenantId,
        UUID propertyId,
        UUID unitId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyRent
) implements DomainEvent {}
