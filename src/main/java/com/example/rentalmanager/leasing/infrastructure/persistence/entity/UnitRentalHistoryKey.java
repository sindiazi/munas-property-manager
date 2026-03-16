package com.example.rentalmanager.leasing.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Composite primary key for {@link UnitRentalHistoryEntity}.
 *
 * <p>Partition key: {@code unit_id} — all history for a unit lives on one partition.
 * <p>Clustering: {@code lease_start DESC} then {@code lease_id ASC} — most recent
 * lease is returned first.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
public class UnitRentalHistoryKey implements Serializable {

    @PrimaryKeyColumn(name = "unit_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID unitId;

    @PrimaryKeyColumn(name = "lease_start", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private LocalDate leaseStart;

    @PrimaryKeyColumn(name = "lease_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private UUID leaseId;
}
