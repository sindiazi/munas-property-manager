package com.example.rentalmanager.billing.domain.event;

import com.example.rentalmanager.billing.domain.valueobject.PaymentId;
import com.example.rentalmanager.billing.domain.valueobject.PaymentMethod;
import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRecordedEvent(
        UUID eventId,
        Instant occurredOn,
        PaymentId paymentId,
        UUID invoiceId,
        UUID tenantId,
        BigDecimal amount,
        PaymentMethod method
) implements DomainEvent {}
