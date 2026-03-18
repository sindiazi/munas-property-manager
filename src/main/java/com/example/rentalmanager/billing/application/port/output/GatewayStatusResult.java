package com.example.rentalmanager.billing.application.port.output;

import java.math.BigDecimal;

public record GatewayStatusResult(
        GatewayPaymentStatus status,
        String resultDescription,
        String receiptNumber,
        BigDecimal amountPaid
) {}
