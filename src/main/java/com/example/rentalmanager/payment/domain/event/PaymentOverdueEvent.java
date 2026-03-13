package com.example.rentalmanager.payment.domain.event;

import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Raised when a payment passes its due date without being fully paid. */
public record PaymentOverdueEvent(
        UUID eventId,
        Instant occurredOn,
        PaymentId paymentId,
        UUID tenantId,
        LocalDate dueDate
) implements DomainEvent {}
