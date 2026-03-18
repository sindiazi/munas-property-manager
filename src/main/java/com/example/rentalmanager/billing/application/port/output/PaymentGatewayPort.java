package com.example.rentalmanager.billing.application.port.output;

import reactor.core.publisher.Mono;

public interface PaymentGatewayPort {
    Mono<GatewayInitiationResult> initiate(GatewayPaymentRequest request);
    Mono<GatewayStatusResult> queryStatus(String gatewayTransactionId);
}
