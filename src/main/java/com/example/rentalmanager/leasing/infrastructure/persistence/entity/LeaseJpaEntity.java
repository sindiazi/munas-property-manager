package com.example.rentalmanager.leasing.infrastructure.persistence.entity;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
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

/** Spring Data Cassandra persistence entity for the {@code leases} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("leases")
public class LeaseJpaEntity {

    @PrimaryKey
    private UUID id;
    @Indexed
    private UUID tenantId;
    private UUID propertyId;
    @Indexed
    private UUID unitId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    @Indexed
    private LeaseStatus status;
    private String terminationReason;
    private Instant createdAt;
}