package com.example.rentalmanager.payment.domain.event;

import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Raised when a payment is fully or partially received. */
public record PaymentReceivedEvent(
        UUID eventId,
        Instant occurredOn,
        PaymentId paymentId,
        UUID tenantId,
        BigDecimal amountPaid
) implements DomainEvent {}
