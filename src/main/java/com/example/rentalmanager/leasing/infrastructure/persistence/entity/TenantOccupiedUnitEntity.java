package com.example.rentalmanager.leasing.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * CQRS read-model projection: the unit a tenant currently occupies.
 * Keyed by tenant_id — single primary-key lookup answers "which unit is tenant X in?".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tenant_occupied_unit")
public class TenantOccupiedUnitEntity {

    @PrimaryKey("tenant_id")
    private UUID tenantId;

    @Column("unit_id")
    private UUID unitId;

    @Column("property_id")
    private UUID propertyId;

    @Column("lease_id")
    private UUID leaseId;

    @Column("monthly_rent")
    private BigDecimal monthlyRent;

    @Column("lease_start")
    private LocalDate leaseStart;

    @Column("lease_end")
    private LocalDate leaseEnd;

    @Column("occupied_since")
    private Instant occupiedSince;
}
