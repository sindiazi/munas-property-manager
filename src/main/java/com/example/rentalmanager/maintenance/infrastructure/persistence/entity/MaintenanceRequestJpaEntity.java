package com.example.rentalmanager.maintenance.infrastructure.persistence.entity;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/** Spring Data R2DBC persistence entity for the {@code maintenance_requests} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("maintenance_requests")
public class MaintenanceRequestJpaEntity {

    @Id
    private UUID id;
    private UUID propertyId;
    private UUID unitId;
    private UUID tenantId;
    private String problemDescription;
    private String resolutionNotes;
    private MaintenancePriority priority;
    private MaintenanceStatus status;
    private Instant requestedAt;
    private Instant completedAt;
}
