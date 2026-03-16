package com.example.rentalmanager.leasing.domain.event;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Raised when a lease expires naturally at the end of its term. */
public record LeaseExpiredEvent(
        UUID eventId,
        Instant occurredOn,
        LeaseId leaseId,
        UUID tenantId,
        UUID unitId,
        UUID propertyId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyRent
) implements DomainEvent {}
