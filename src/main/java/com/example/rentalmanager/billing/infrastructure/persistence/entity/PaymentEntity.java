package com.example.rentalmanager.billing.infrastructure.persistence.entity;

import com.example.rentalmanager.billing.domain.valueobject.PaymentMethod;
import com.example.rentalmanager.billing.domain.valueobject.PaymentTransactionStatus;
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
@Table("payments")
public class PaymentEntity {

    @PrimaryKey
    private UUID id;

    @Indexed
    @Column("invoice_id")
    private UUID invoiceId;

    @Column("tenant_id")
    private UUID tenantId;

    private BigDecimal amount;

    @Column("currency_code")
    private String currencyCode;

    private PaymentMethod method;

    private PaymentTransactionStatus status;

    private String reference;

    @Column("payment_date")
    private LocalDate paymentDate;

    @Column("created_at")
    private Instant createdAt;
}
