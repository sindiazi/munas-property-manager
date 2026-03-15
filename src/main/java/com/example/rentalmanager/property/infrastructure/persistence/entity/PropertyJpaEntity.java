package com.example.rentalmanager.property.infrastructure.persistence.entity;

import com.example.rentalmanager.property.domain.valueobject.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Spring Data Cassandra persistence entity for the {@code properties} table.
 *
 * <p>Named {@code JpaEntity} to satisfy the package convention requested in
 * the requirements; the actual store is Apache Cassandra accessed reactively
 * via Spring Data Cassandra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("properties")
public class PropertyJpaEntity {

    @PrimaryKey
    private UUID id;
    @Indexed
    private UUID ownerId;
    private String name;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private PropertyType type;
    private Instant createdAt;
}
