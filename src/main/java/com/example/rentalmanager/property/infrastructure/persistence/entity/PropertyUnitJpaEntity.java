package com.example.rentalmanager.property.infrastructure.persistence.entity;

import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** Spring Data Cassandra persistence entity for the {@code property_units} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("property_units")
public class PropertyUnitJpaEntity {

    @PrimaryKey
    private UUID id;
    @Indexed
    @Column("property_id")
    private UUID propertyId;
    @Column("unit_number")
    private String unitNumber;
    private int bedrooms;
    private int bathrooms;
    @Column("square_footage")
    private double squareFootage;
    @Column("monthly_rent_amount")
    private BigDecimal monthlyRentAmount;
    @Column("currency_code")
    private String currencyCode;
    @Indexed
    private UnitStatus status;
}