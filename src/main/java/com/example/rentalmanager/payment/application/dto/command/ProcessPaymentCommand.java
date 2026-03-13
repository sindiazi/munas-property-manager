package com.example.rentalmanager.payment.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Command to record receipt of a (possibly partial) payment. */
public record ProcessPaymentCommand(
        @NotNull UUID paymentId,
        @NotNull @Positive BigDecimal amountPaid,
        @NotNull String currencyCode,
        @NotNull LocalDate paymentDate
) {}
