package com.example.rentalmanager.leasing.infrastructure.persistence.mapper;

import com.example.rentalmanager.leasing.domain.aggregate.Lease;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseTerm;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.LeaseJpaEntity;
import org.springframework.stereotype.Component;

/** Anti-corruption mapper between {@link Lease} domain object and R2DBC entity. */
@Component
public class LeasePersistenceMapper {

    public LeaseJpaEntity toEntity(Lease lease) {
        return LeaseJpaEntity.builder()
                .id(lease.getId().value())
                .tenantId(lease.getTenantId())
                .propertyId(lease.getPropertyId())
                .unitId(lease.getUnitId())
                .startDate(lease.getTerm().startDate())
                .endDate(lease.getTerm().endDate())
                .monthlyRent(lease.getMonthlyRent())
                .securityDeposit(lease.getSecurityDeposit())
                .status(lease.getStatus())
                .terminationReason(lease.getTerminationReason())
                .createdAt(lease.getCreatedAt())
                .build();
    }

    public Lease toDomain(LeaseJpaEntity e) {
        return new Lease(
                LeaseId.of(e.getId()),
                e.getTenantId(), e.getPropertyId(), e.getUnitId(),
                new LeaseTerm(e.getStartDate(), e.getEndDate()),
                e.getMonthlyRent(), e.getSecurityDeposit(),
                e.getStatus(), e.getTerminationReason(), e.getCreatedAt());
    }
}
