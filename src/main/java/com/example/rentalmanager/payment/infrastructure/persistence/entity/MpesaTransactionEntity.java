package com.example.rentalmanager.payment.infrastructure.persistence.entity;

import com.example.rentalmanager.payment.domain.valueobject.MpesaTransactionStatus;
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
import java.util.UUID;

/** Spring Data Cassandra entity for the {@code mpesa_transactions} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mpesa_transactions")
public class MpesaTransactionEntity {

    @PrimaryKey
    @Column("checkout_request_id")
    private String checkoutRequestId;

    @Column("merchant_request_id")
    private String merchantRequestId;

    @Indexed
    @Column("payment_id")
    private UUID paymentId;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("phone_number")
    private String phoneNumber;

    private BigDecimal amount;

    private MpesaTransactionStatus status;

    @Column("mpesa_receipt_number")
    private String mpesaReceiptNumber;

    @Column("initiated_at")
    private Instant initiatedAt;

    @Column("completed_at")
    private Instant completedAt;
}
