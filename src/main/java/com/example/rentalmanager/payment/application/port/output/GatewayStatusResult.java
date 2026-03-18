package com.example.rentalmanager.payment.application.port.output;

import java.math.BigDecimal;

/** Result returned when querying the status of a previously initiated payment. */
public record GatewayStatusResult(
        String gatewayTransactionId,
        GatewayPaymentStatus status,
        String resultDescription,
        String receiptNumber,
        BigDecimal amountPaid,
        String phoneNumber
) {}
