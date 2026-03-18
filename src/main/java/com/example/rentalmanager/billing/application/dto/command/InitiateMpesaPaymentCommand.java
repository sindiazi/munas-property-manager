package com.example.rentalmanager.billing.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record InitiateMpesaPaymentCommand(
        @NotNull UUID invoiceId,
        @NotNull @Positive BigDecimal amount,
        @NotNull String phoneNumber
) {}
