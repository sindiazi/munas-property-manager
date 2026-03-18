package com.example.rentalmanager.payment.application.port.output;

/** Result returned by the gateway after successfully initiating a payment request. */
public record GatewayInitiationResult(
        String gatewayTransactionId,
        String merchantRequestId,
        String customerMessage
) {}
