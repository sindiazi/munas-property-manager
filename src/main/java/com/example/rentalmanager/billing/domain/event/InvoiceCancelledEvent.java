package com.example.rentalmanager.billing.domain.event;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvoiceCancelledEvent(
        UUID eventId,
        Instant occurredOn,
        InvoiceId invoiceId,
        UUID tenantId
) implements DomainEvent {}
