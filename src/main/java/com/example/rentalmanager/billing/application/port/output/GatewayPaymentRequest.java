package com.example.rentalmanager.billing.application.port.output;

import java.math.BigDecimal;
import java.util.UUID;

public record GatewayPaymentRequest(
        UUID paymentTransactionId,
        UUID invoiceId,
        String phoneNumber,
        BigDecimal amount,
        String currencyCode,
        String accountReference
) {}
