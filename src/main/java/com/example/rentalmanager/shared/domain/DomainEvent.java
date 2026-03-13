package com.example.rentalmanager.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Sealed marker interface for all Domain Events.
 *
 * <p>Every concrete event must be a {@code record} that implements this
 * interface and is listed as a permitted subtype. This ensures exhaustive
 * pattern matching at the switch level.
 *
 * <p>Events are named in the past tense (e.g. {@code PropertyCreated},
 * {@code LeaseActivated}) and are immutable by design.
 */
public sealed interface DomainEvent
        permits
        // property
        com.example.rentalmanager.property.domain.event.PropertyCreatedEvent,
        com.example.rentalmanager.property.domain.event.PropertyUnitAddedEvent,
        com.example.rentalmanager.property.domain.event.UnitStatusChangedEvent,
        // tenant
        com.example.rentalmanager.tenant.domain.event.TenantRegisteredEvent,
        com.example.rentalmanager.tenant.domain.event.TenantStatusChangedEvent,
        // leasing
        com.example.rentalmanager.leasing.domain.event.LeaseCreatedEvent,
        com.example.rentalmanager.leasing.domain.event.LeaseActivatedEvent,
        com.example.rentalmanager.leasing.domain.event.LeaseTerminatedEvent,
        // payment
        com.example.rentalmanager.payment.domain.event.PaymentCreatedEvent,
        com.example.rentalmanager.payment.domain.event.PaymentReceivedEvent,
        com.example.rentalmanager.payment.domain.event.PaymentOverdueEvent,
        // maintenance
        com.example.rentalmanager.maintenance.domain.event.MaintenanceRequestCreatedEvent,
        com.example.rentalmanager.maintenance.domain.event.MaintenanceRequestStatusChangedEvent {

    /** Globally unique event identifier. */
    UUID eventId();

    /** Wall-clock time when the event was raised. */
    Instant occurredOn();
}
