package com.example.rentalmanager.payment.application.port.output;

import reactor.core.publisher.Mono;

/**
 * Secondary port for payment gateway operations.
 * Implemented by gateway-specific adapters (e.g. M-Pesa, Stripe).
 * Adding a new gateway requires only a new implementation — no application-layer changes.
 */
public interface PaymentGatewayPort {
    Mono<GatewayInitiationResult> initiate(GatewayPaymentRequest request);
    Mono<GatewayStatusResult>     queryStatus(String gatewayTransactionId);
}
