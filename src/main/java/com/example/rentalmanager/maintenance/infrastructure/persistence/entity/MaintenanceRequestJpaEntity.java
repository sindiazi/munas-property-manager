package com.example.rentalmanager.maintenance.infrastructure.persistence.entity;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
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
    @Column("property_id")
    private UUID propertyId;
    @Column("unit_id")
    private UUID unitId;
    @Indexed
    @Column("tenant_id")
    private UUID tenantId;
    @Column("problem_description")
    private String problemDescription;
    @Column("resolution_notes")
    private String resolutionNotes;
    private MaintenancePriority priority;
    @Indexed
    private MaintenanceStatus status;
    @Column("requested_at")
    private Instant requestedAt;
    @Column("completed_at")
    private Instant completedAt;
}