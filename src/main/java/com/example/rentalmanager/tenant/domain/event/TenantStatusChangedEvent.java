package com.example.rentalmanager.tenant.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;

import java.time.Instant;
import java.util.UUID;

/** Raised when a tenant's status transitions. */
public record TenantStatusChangedEvent(
        UUID eventId,
        Instant occurredOn,
        TenantId tenantId,
        TenantStatus previousStatus,
        TenantStatus newStatus
) implements DomainEvent {}
