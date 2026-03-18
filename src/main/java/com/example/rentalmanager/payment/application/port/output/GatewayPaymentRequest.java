package com.example.rentalmanager.payment.application.port.output;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gateway-agnostic payment initiation request.
 * Used by {@link PaymentGatewayPort} to decouple the application layer from M-Pesa specifics.
 */
public record GatewayPaymentRequest(
        UUID paymentId,
        UUID leaseId,
        String phoneNumber,
        BigDecimal amount,
        String currencyCode,
        String accountReference
) {}
