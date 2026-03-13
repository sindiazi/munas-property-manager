package com.example.rentalmanager.payment.application.dto.response;

import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import com.example.rentalmanager.payment.domain.valueobject.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Read-model DTO for a payment record. */
public record PaymentResponse(
        UUID id,
        UUID leaseId,
        UUID tenantId,
        BigDecimal amountDue,
        BigDecimal amountPaid,
        BigDecimal outstandingBalance,
        String currencyCode,
        LocalDate dueDate,
        LocalDate paidDate,
        PaymentStatus status,
        PaymentType type,
        Instant createdAt
) {}
