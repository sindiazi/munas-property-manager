package com.example.rentalmanager.leasing.infrastructure.persistence.entity;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
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
    @Column("tenant_id")
    private UUID tenantId;
    @Column("property_id")
    private UUID propertyId;
    @Indexed
    @Column("unit_id")
    private UUID unitId;
    @Column("start_date")
    private LocalDate startDate;
    @Column("end_date")
    private LocalDate endDate;
    @Column("monthly_rent")
    private BigDecimal monthlyRent;
    @Column("security_deposit")
    private BigDecimal securityDeposit;
    @Indexed
    private LeaseStatus status;
    @Column("termination_reason")
    private String terminationReason;
    @Column("created_at")
    private Instant createdAt;
}