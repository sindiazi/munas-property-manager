package com.example.rentalmanager.billing.application.dto.command;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateInvoiceCommand(
        @NotNull UUID leaseId,
        @NotNull UUID tenantId,
        @NotNull @Positive BigDecimal amount,
        @NotNull String currencyCode,
        @NotNull LocalDate dueDate,
        @NotNull InvoiceType type
) {}
