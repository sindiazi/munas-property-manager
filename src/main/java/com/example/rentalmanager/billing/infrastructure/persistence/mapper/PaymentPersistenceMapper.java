package com.example.rentalmanager.billing.infrastructure.persistence.mapper;

import com.example.rentalmanager.billing.domain.aggregate.Payment;
import com.example.rentalmanager.billing.domain.valueobject.Money;
import com.example.rentalmanager.billing.domain.valueobject.PaymentId;
import com.example.rentalmanager.billing.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentPersistenceMapper {

    public PaymentEntity toEntity(Payment p) {
        return PaymentEntity.builder()
                .id(p.getId().value())
                .invoiceId(p.getInvoiceId())
                .tenantId(p.getTenantId())
                .amount(p.getAmount().amount())
                .currencyCode(p.getAmount().currency().getCurrencyCode())
                .method(p.getMethod())
                .status(p.getStatus())
                .reference(p.getReference())
                .paymentDate(p.getPaymentDate())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public Payment toDomain(PaymentEntity e) {
        return new Payment(
                PaymentId.of(e.getId()),
                e.getInvoiceId(), e.getTenantId(),
                Money.of(e.getAmount(), e.getCurrencyCode()),
                e.getMethod(), e.getStatus(), e.getReference(),
                e.getPaymentDate(), e.getCreatedAt());
    }
}
