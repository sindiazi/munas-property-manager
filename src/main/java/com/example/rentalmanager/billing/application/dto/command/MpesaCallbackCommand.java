package com.example.rentalmanager.billing.application.dto.command;

import java.math.BigDecimal;
import java.time.Instant;

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
