package com.example.rentalmanager.billing.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MpesaPaymentInitiatedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID invoiceId,
        UUID tenantId,
        String phoneNumber,
        BigDecimal amount
) implements DomainEvent {}
