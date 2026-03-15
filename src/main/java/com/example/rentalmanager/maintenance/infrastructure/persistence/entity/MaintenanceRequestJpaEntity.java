package com.example.rentalmanager.maintenance.infrastructure.persistence.entity;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/** Spring Data Cassandra persistence entity for the {@code maintenance_requests} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("maintenance_requests")
public class MaintenanceRequestJpaEntity {

    @PrimaryKey
    private UUID id;
    @Indexed
    private UUID propertyId;
    private UUID unitId;
    @Indexed
    private UUID tenantId;
    private String problemDescription;
    private String resolutionNotes;
    private MaintenancePriority priority;
    @Indexed
    private MaintenanceStatus status;
    private Instant requestedAt;
    private Instant completedAt;
}