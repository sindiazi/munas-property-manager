package com.example.rentalmanager.property.infrastructure.persistence.entity;

import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** Spring Data R2DBC persistence entity for the {@code property_units} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("property_units")
public class PropertyUnitJpaEntity {

    @Id
    private UUID id;
    private UUID propertyId;
    private String unitNumber;
    private int bedrooms;
    private int bathrooms;
    private double squareFootage;
    private BigDecimal monthlyRentAmount;
    private String currencyCode;
    private UnitStatus status;
}
