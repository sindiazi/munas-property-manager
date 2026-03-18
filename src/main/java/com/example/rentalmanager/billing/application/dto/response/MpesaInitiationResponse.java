package com.example.rentalmanager.billing.application.dto.response;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;

import java.util.UUID;

public record MpesaInitiationResponse(
        UUID invoiceId,
        UUID paymentTransactionId,
        String checkoutRequestId,
        String merchantRequestId,
        String customerMessage,
        InvoiceStatus invoiceStatus
) {}
