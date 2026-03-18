package com.example.rentalmanager.billing.domain.event;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceId;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceType;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        InvoiceId invoiceId,
        UUID leaseId,
        UUID tenantId,
        BigDecimal amount,
        InvoiceType type
) implements DomainEvent {}
