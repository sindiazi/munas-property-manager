package com.example.rentalmanager.billing.application.dto.response;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID invoiceId,
        UUID leaseId,
        UUID tenantId,
        BigDecimal amountDue,
        BigDecimal amountPaid,
        BigDecimal outstandingBalance,
        String currencyCode,
        LocalDate dueDate,
        LocalDate paidDate,
        InvoiceStatus status,
        InvoiceType type,
        Instant createdAt
) {}
