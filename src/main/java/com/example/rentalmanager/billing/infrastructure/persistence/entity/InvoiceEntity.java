package com.example.rentalmanager.billing.infrastructure.persistence.entity;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("invoices")
public class InvoiceEntity {

    @PrimaryKey
    private UUID id;

    @Indexed
    @Column("lease_id")
    private UUID leaseId;

    @Indexed
    @Column("tenant_id")
    private UUID tenantId;

    @Column("amount_due")
    private BigDecimal amountDue;

    @Column("amount_paid")
    private BigDecimal amountPaid;

    @Column("currency_code")
    private String currencyCode;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("paid_date")
    private LocalDate paidDate;

    @Indexed
    private InvoiceStatus status;

    private InvoiceType type;

    @Column("created_at")
    private Instant createdAt;
}
