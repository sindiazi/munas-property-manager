package com.example.rentalmanager.billing.application.dto.response;

import com.example.rentalmanager.billing.domain.valueobject.PaymentMethod;
import com.example.rentalmanager.billing.domain.valueobject.PaymentTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentTransactionResponse(
        UUID id,
        UUID invoiceId,
        UUID tenantId,
        BigDecimal amount,
        String currencyCode,
        PaymentMethod method,
        PaymentTransactionStatus status,
        String reference,
        LocalDate paymentDate,
        Instant createdAt
) {}
