package com.example.rentalmanager.leasing.infrastructure.persistence.entity;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Spring Data R2DBC persistence entity for the {@code leases} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("leases")
public class LeaseJpaEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID propertyId;
    private UUID unitId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private LeaseStatus status;
    private String terminationReason;
    private Instant createdAt;
}
