package com.example.rentalmanager.payment.application.dto.command;

import java.math.BigDecimal;
import java.time.Instant;

/** Assembled from Daraja callback JSON in the controller — not user input, no validation. */
public record MpesaCallbackCommand(
        String checkoutRequestId,
        String merchantRequestId,
        int resultCode,
        String resultDesc,
        String mpesaReceiptNumber,
        BigDecimal amount,
        String phoneNumber,
        Instant transactionDate
) {}
