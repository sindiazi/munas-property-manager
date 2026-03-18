package com.example.rentalmanager.payment.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Raised when M-Pesa reports a failure or cancellation via callback. */
public record MpesaPaymentFailedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID paymentId,
        UUID tenantId,
        int resultCode,
        String resultDesc
) implements DomainEvent {}
