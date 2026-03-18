package com.example.rentalmanager.payment.infrastructure.persistence.mapper;

import com.example.rentalmanager.payment.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.payment.infrastructure.persistence.entity.MpesaTransactionEntity;
import org.springframework.stereotype.Component;

/** Maps between {@link MpesaTransaction} domain entity and Cassandra entity. */
@Component
public class MpesaTransactionMapper {

    public MpesaTransactionEntity toEntity(MpesaTransaction tx) {
        return MpesaTransactionEntity.builder()
                .checkoutRequestId(tx.getCheckoutRequestId())
                .merchantRequestId(tx.getMerchantRequestId())
                .paymentId(tx.getPaymentId())
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
                e.getCheckoutRequestId(),
                e.getMerchantRequestId(),
                e.getPaymentId(),
                e.getTenantId(),
                e.getPhoneNumber(),
                e.getAmount(),
                e.getStatus(),
                e.getMpesaReceiptNumber(),
                e.getInitiatedAt(),
                e.getCompletedAt());
    }
}
