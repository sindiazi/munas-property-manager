package com.example.rentalmanager.payment.domain.event;

import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.payment.domain.valueobject.PaymentType;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Raised when a payment record is created. */
public record PaymentCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        PaymentId paymentId,
        UUID leaseId,
        UUID tenantId,
        BigDecimal amount,
        PaymentType type
) implements DomainEvent {}
