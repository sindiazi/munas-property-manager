package com.example.rentalmanager.property.infrastructure.persistence.mapper;

import com.example.rentalmanager.property.domain.aggregate.Property;
import com.example.rentalmanager.property.domain.aggregate.PropertyUnit;
import com.example.rentalmanager.property.domain.valueobject.*;
import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyJpaEntity;
import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyUnitJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Anti-corruption mapper between the domain model and the persistence model.
 *
 * <p>Hand-written for full control over the translation. MapStruct could be
 * used for boilerplate reduction on simpler mappings.
 */
@Component
public class PropertyPersistenceMapper {

    // ── Domain → Persistence ───────────────────────────────────────────────

    public PropertyJpaEntity toEntity(Property property) {
        return PropertyJpaEntity.builder()
                .id(property.getId().value())
                .ownerId(property.getOwnerId().value())
                .name(property.getName())
                .street(property.getAddress().street())
                .city(property.getAddress().city())
                .state(property.getAddress().state())
                .zipCode(property.getAddress().zipCode())
                .country(property.getAddress().country())
                .type(property.getType())
                .createdAt(property.getCreatedAt())
                .build();
    }

    public PropertyUnitJpaEntity toUnitEntity(PropertyUnit unit, PropertyId propertyId) {
        return PropertyUnitJpaEntity.builder()
                .id(unit.getId().value())
                .propertyId(propertyId.value())
                .unitNumber(unit.getUnitNumber())
                .bedrooms(unit.getBedrooms())
                .bathrooms(unit.getBathrooms())
                .squareFootage(unit.getSquareFootage())
                .monthlyRentAmount(unit.getMonthlyRent().amount())
                .currencyCode(unit.getMonthlyRent().currency().getCurrencyCode())
                .status(unit.getStatus())
                .build();
    }

    // ── Persistence → Domain ───────────────────────────────────────────────

    public Property toDomain(PropertyJpaEntity entity, List<PropertyUnitJpaEntity> unitEntities) {
        var units = unitEntities.stream().map(this::toDomainUnit).toList();
        return new Property(
                PropertyId.of(entity.getId()),
                OwnerId.of(entity.getOwnerId()),
                entity.getName(),
                new Address(entity.getStreet(), entity.getCity(),
                        entity.getState(), entity.getZipCode(), entity.getCountry()),
                entity.getType(),
                units,
                entity.getCreatedAt());
    }

    public PropertyUnit toDomainUnit(PropertyUnitJpaEntity entity) {
        return new PropertyUnit(
                UnitId.of(entity.getId()),
                entity.getUnitNumber(),
                entity.getBedrooms(),
                entity.getBathrooms(),
                entity.getSquareFootage(),
                MonthlyRent.of(entity.getMonthlyRentAmount(), entity.getCurrencyCode()));
    }
}
