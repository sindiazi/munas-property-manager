package com.example.rentalmanager.billing.infrastructure.persistence.mapper;

import com.example.rentalmanager.billing.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.billing.infrastructure.persistence.entity.MpesaTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class MpesaTransactionMapper {

    public MpesaTransactionEntity toEntity(MpesaTransaction tx) {
        return MpesaTransactionEntity.builder()
                .checkoutRequestId(tx.getCheckoutRequestId())
                .merchantRequestId(tx.getMerchantRequestId())
                .paymentTransactionId(tx.getPaymentTransactionId())
                .tenantId(tx.getTenantId())
                .phoneNumber(tx.getPhoneNumber())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .mpesaReceiptNumber(tx.getMpesaReceiptNumber())
                .initiatedAt(tx.getInitiatedAt())
                .completedAt(tx.getCompletedAt())
                .build();
    }

    public MpesaTransaction toDomain(MpesaTransactionEntity e) {
        return new MpesaTransaction(
                e.getCheckoutRequestId(), e.getMerchantRequestId(),
                e.getPaymentTransactionId(), e.getTenantId(), e.getPhoneNumber(),
                e.getAmount(), e.getStatus(), e.getMpesaReceiptNumber(),
                e.getInitiatedAt(), e.getCompletedAt());
    }
}
