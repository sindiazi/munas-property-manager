package com.example.rentalmanager.leasing.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * CQRS read-model projection: full rental history for a unit.
 * Partitioned by unit_id, clustered by lease_start DESC — single-partition scan,
 * no ALLOW FILTERING needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("unit_rental_history")
public class UnitRentalHistoryEntity {

    @PrimaryKey
    private UnitRentalHistoryKey key;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("property_id")
    private UUID propertyId;

    @Column("monthly_rent")
    private BigDecimal monthlyRent;

    @Column("lease_end")
    private LocalDate leaseEnd;

    /** Denormalised lease status: ACTIVE | TERMINATED | EXPIRED */
    private String status;
}
