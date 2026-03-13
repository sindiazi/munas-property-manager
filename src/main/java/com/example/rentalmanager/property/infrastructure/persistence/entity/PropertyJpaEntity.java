package com.example.rentalmanager.property.infrastructure.persistence.entity;

import com.example.rentalmanager.property.domain.valueobject.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Spring Data R2DBC persistence entity for the {@code properties} table.
 *
 * <p>Named {@code JpaEntity} to satisfy the package convention requested in
 * the requirements; the actual ORM technology used is R2DBC (reactive),
 * which is necessary for non-blocking I/O with Spring Reactor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("properties")
public class PropertyJpaEntity {

    @Id
    private UUID id;
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
