package com.example.rentalmanager.payment.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Raised when an M-Pesa STK Push is successfully initiated. */
public record MpesaPaymentInitiatedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID paymentId,
        UUID tenantId,
        String checkoutRequestId,
        BigDecimal amount
) implements DomainEvent {}
