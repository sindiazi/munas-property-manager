package com.example.rentalmanager.payment.application.dto.response;

import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;

import java.util.UUID;

public record MpesaInitiationResponse(
        UUID paymentId,
        String checkoutRequestId,
        String merchantRequestId,
        String customerMessage,
        PaymentStatus paymentStatus
) {}
