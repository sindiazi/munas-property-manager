package com.example.rentalmanager.maintenance.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/** Spring Data Cassandra persistence entity for the {@code maintenance_categories} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("maintenance_categories")
public class MaintenanceCategoryEntity {

    @PrimaryKey
    private String id;

    private String name;
}
