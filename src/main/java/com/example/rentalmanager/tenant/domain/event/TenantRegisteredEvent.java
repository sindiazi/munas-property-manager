package com.example.rentalmanager.tenant.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;

import java.time.Instant;
import java.util.UUID;

/** Raised when a new tenant registers in the system. */
public record TenantRegisteredEvent(
        UUID eventId,
        Instant occurredOn,
        TenantId tenantId,
        String fullName,
        String email
) implements DomainEvent {}
