package com.example.rentalmanager.billing.domain.event;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceSettledEvent(
        UUID eventId,
        Instant occurredOn,
        InvoiceId invoiceId,
        UUID tenantId,
        BigDecimal amountReceived
) implements DomainEvent {}
