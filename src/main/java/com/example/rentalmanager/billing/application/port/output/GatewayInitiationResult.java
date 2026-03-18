package com.example.rentalmanager.billing.application.port.output;

public record GatewayInitiationResult(
        String gatewayTransactionId,
        String merchantRequestId,
        String customerMessage
) {}
