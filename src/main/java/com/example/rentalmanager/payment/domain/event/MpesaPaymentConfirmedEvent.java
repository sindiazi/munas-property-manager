package com.example.rentalmanager.payment.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Raised when M-Pesa confirms a payment via callback (ResultCode = 0). */
public record MpesaPaymentConfirmedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID paymentId,
        UUID tenantId,
        String mpesaReceiptNumber,
        BigDecimal amountPaid,
        String phoneNumber
) implements DomainEvent {}
