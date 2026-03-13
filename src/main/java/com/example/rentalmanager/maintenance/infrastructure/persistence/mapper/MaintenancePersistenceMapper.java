package com.example.rentalmanager.maintenance.infrastructure.persistence.mapper;

import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceRequest;
import com.example.rentalmanager.maintenance.domain.valueobject.RequestId;
import com.example.rentalmanager.maintenance.domain.valueobject.WorkDescription;
import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceRequestJpaEntity;
import org.springframework.stereotype.Component;

/** Anti-corruption mapper for the Maintenance bounded context. */
@Component
public class MaintenancePersistenceMapper {

    public MaintenanceRequestJpaEntity toEntity(MaintenanceRequest r) {
        return MaintenanceRequestJpaEntity.builder()
                .id(r.getId().value())
                .propertyId(r.getPropertyId())
                .unitId(r.getUnitId())
                .tenantId(r.getTenantId())
                .problemDescription(r.getDescription().problemDescription())
                .resolutionNotes(r.getDescription().resolutionNotes())
                .priority(r.getPriority())
                .status(r.getStatus())
                .requestedAt(r.getRequestedAt())
                .completedAt(r.getCompletedAt())
                .build();
    }

    public MaintenanceRequest toDomain(MaintenanceRequestJpaEntity e) {
        return new MaintenanceRequest(
                RequestId.of(e.getId()),
                e.getPropertyId(), e.getUnitId(), e.getTenantId(),
                new WorkDescription(e.getProblemDescription(), e.getResolutionNotes()),
                e.getPriority(), e.getStatus(), e.getRequestedAt(), e.getCompletedAt());
    }
}
