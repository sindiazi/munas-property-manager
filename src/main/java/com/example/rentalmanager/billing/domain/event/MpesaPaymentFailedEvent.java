package com.example.rentalmanager.billing.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MpesaPaymentFailedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID invoiceId,
        UUID tenantId,
        int resultCode,
        String resultDesc
) implements DomainEvent {}
