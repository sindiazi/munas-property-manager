package com.example.rentalmanager.property.domain.service;

import com.example.rentalmanager.property.domain.aggregate.Property;
import com.example.rentalmanager.property.domain.aggregate.PropertyUnit;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;

/**
 * Domain Service for property-related business rules that do not naturally
 * belong to a single aggregate instance.
 *
 * <p>Stateless — does not hold any fields and has no Spring dependency.
 */
public class PropertyDomainService {

    /**
     * Calculates the occupancy rate of a property as a percentage.
     *
     * @param property the property to evaluate
     * @return occupancy percentage in [0, 100]
     */
    public double calculateOccupancyRate(Property property) {
        var units = property.getUnits();
        if (units.isEmpty()) return 0.0;

        long occupied = units.stream()
                .filter(u -> u.getStatus() == UnitStatus.OCCUPIED)
                .count();

        return (double) occupied / units.size() * 100;
    }

    /**
     * Determines whether a property has at least one available unit for rental.
     */
    public boolean hasAvailableUnits(Property property) {
        return property.getUnits().stream()
                .anyMatch(u -> u.getStatus() == UnitStatus.AVAILABLE);
    }

    /**
     * Calculates the total potential monthly revenue for a fully occupied property.
     */
    public java.math.BigDecimal calculatePotentialMonthlyRevenue(Property property) {
        return property.getUnits().stream()
                .map(PropertyUnit::getMonthlyRent)
                .map(rent -> rent.amount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
