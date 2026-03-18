package com.example.rentalmanager.payment.application.dto.response;

import com.example.rentalmanager.payment.domain.valueobject.MpesaTransactionStatus;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record MpesaStatusResponse(
        UUID paymentId,
        String checkoutRequestId,
        MpesaTransactionStatus transactionStatus,
        String resultDescription,
        String mpesaReceiptNumber,
        BigDecimal amountPaid,
        PaymentStatus paymentStatus
) {}
