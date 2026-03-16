package com.example.rentalmanager.property.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Spring Data Cassandra persistence entity for the {@code unit_unavailability} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("unit_unavailability")
public class UnitUnavailabilityJpaEntity {

    @PrimaryKey
    private UUID id;

    @Indexed
    private UUID unitId;

    private String    reason;
    private LocalDate startDate;
    private LocalDate endDate;      // null = open-ended
    private Instant   createdAt;
}
