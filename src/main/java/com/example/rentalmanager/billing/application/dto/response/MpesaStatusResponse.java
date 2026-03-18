package com.example.rentalmanager.billing.application.dto.response;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.billing.domain.valueobject.MpesaTransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record MpesaStatusResponse(
        UUID invoiceId,
        String checkoutRequestId,
        MpesaTransactionStatus transactionStatus,
        String resultDescription,
        String mpesaReceiptNumber,
        BigDecimal amountPaid,
        InvoiceStatus invoiceStatus
) {}
