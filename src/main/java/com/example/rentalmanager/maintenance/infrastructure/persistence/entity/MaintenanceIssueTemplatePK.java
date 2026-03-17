package com.example.rentalmanager.maintenance.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;

/**
 * Composite primary key for {@code maintenance_issue_templates}.
 * Partitioning by {@code category_id} allows all issues for a category to be
 * fetched and deleted as a single partition operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
public class MaintenanceIssueTemplatePK implements Serializable {

    @PrimaryKeyColumn(name = "category_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String categoryId;

    @PrimaryKeyColumn(name = "id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String id;
}
