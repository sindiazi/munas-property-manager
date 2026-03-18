package com.example.rentalmanager.billing.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordCashPaymentCommand(
        @NotNull UUID invoiceId,
        @NotNull @Positive BigDecimal amountPaid,
        @NotNull LocalDate paymentDate
) {}
