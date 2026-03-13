package com.example.rentalmanager.payment.infrastructure.persistence.mapper;

import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.Money;
import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

/** Anti-corruption mapper between {@link Payment} domain object and R2DBC entity. */
@Component
public class PaymentPersistenceMapper {

    public PaymentJpaEntity toEntity(Payment p) {
        return PaymentJpaEntity.builder()
                .id(p.getId().value())
                .leaseId(p.getLeaseId())
                .tenantId(p.getTenantId())
                .amountDue(p.getAmountDue().amount())
                .amountPaid(p.getAmountPaid().amount())
                .currencyCode(p.getAmountDue().currency().getCurrencyCode())
                .dueDate(p.getDueDate())
                .paidDate(p.getPaidDate())
                .status(p.getStatus())
                .type(p.getType())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public Payment toDomain(PaymentJpaEntity e) {
        return new Payment(
                PaymentId.of(e.getId()),
                e.getLeaseId(), e.getTenantId(),
                Money.of(e.getAmountDue(), e.getCurrencyCode()),
                Money.of(e.getAmountPaid(), e.getCurrencyCode()),
                e.getDueDate(), e.getPaidDate(),
                e.getStatus(), e.getType(), e.getCreatedAt());
    }
}
