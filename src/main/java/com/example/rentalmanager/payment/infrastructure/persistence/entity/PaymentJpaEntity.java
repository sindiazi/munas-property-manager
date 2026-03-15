package com.example.rentalmanager.payment.infrastructure.persistence.entity;

import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import com.example.rentalmanager.payment.domain.valueobject.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Spring Data Cassandra persistence entity for the {@code payments} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payments")
public class PaymentJpaEntity {

    @PrimaryKey
    private UUID id;
    @Indexed
    private UUID leaseId;
    @Indexed
    private UUID tenantId;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private String currencyCode;
    private LocalDate dueDate;
    private LocalDate paidDate;
    @Indexed
    private PaymentStatus status;
    private PaymentType type;
    private Instant createdAt;
}