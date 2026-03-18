package com.example.rentalmanager.billing.infrastructure.persistence.mapper;

import com.example.rentalmanager.billing.domain.aggregate.Invoice;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceId;
import com.example.rentalmanager.billing.domain.valueobject.Money;
import com.example.rentalmanager.billing.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.stereotype.Component;

@Component
public class InvoicePersistenceMapper {

    public InvoiceEntity toEntity(Invoice inv) {
        return InvoiceEntity.builder()
                .id(inv.getId().value())
                .leaseId(inv.getLeaseId())
                .tenantId(inv.getTenantId())
                .amountDue(inv.getAmountDue().amount())
                .amountPaid(inv.getAmountPaid().amount())
                .currencyCode(inv.getAmountDue().currency().getCurrencyCode())
                .dueDate(inv.getDueDate())
                .paidDate(inv.getPaidDate())
                .status(inv.getStatus())
                .type(inv.getType())
                .createdAt(inv.getCreatedAt())
                .build();
    }

    public Invoice toDomain(InvoiceEntity e) {
        return new Invoice(
                InvoiceId.of(e.getId()),
                e.getLeaseId(), e.getTenantId(),
                Money.of(e.getAmountDue(), e.getCurrencyCode()),
                Money.of(e.getAmountPaid(), e.getCurrencyCode()),
                e.getDueDate(), e.getPaidDate(),
                e.getStatus(), e.getType(), e.getCreatedAt());
    }
}
