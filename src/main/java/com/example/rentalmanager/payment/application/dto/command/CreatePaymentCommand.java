package com.example.rentalmanager.payment.application.dto.command;

import com.example.rentalmanager.payment.domain.valueobject.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Command to create a new payment obligation. */
public record CreatePaymentCommand(
        @NotNull UUID leaseId,
        @NotNull UUID tenantId,
        @NotNull @Positive BigDecimal amount,
        @NotNull String currencyCode,
        @NotNull LocalDate dueDate,
        @NotNull PaymentType type
) {}
