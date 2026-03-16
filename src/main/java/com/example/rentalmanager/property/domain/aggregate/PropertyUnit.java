package com.example.rentalmanager.property.domain.aggregate;

import com.example.rentalmanager.property.domain.valueobject.MonthlyRent;
import com.example.rentalmanager.property.domain.valueobject.UnitId;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import lombok.Getter;

/**
 * Entity within the {@link Property} aggregate representing a single rentable unit.
 *
 * <p>All state mutations go through methods that enforce invariants and return
 * new instances where immutability is required. Mutable state is encapsulated
 * behind business-method boundaries.
 */
@Getter
public class PropertyUnit {

    private final UnitId id;
    private final String unitNumber;
    private final int bedrooms;
    private final int bathrooms;
    private final double squareFootage;
    private MonthlyRent monthlyRent;
    private UnitStatus status;

    public PropertyUnit(
            UnitId id,
            String unitNumber,
            int bedrooms,
            int bathrooms,
            double squareFootage,
            MonthlyRent monthlyRent) {
        this(id, unitNumber, bedrooms, bathrooms, squareFootage, monthlyRent, UnitStatus.AVAILABLE);
    }

    /** Reconstitution constructor — used by the persistence layer to restore full state. */
    public PropertyUnit(
            UnitId id,
            String unitNumber,
            int bedrooms,
            int bathrooms,
            double squareFootage,
            MonthlyRent monthlyRent,
            UnitStatus status) {

        if (unitNumber == null || unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit number must not be blank");
        }
        if (bedrooms < 0 || bathrooms < 0) {
            throw new IllegalArgumentException("Bedroom/bathroom count cannot be negative");
        }
        if (squareFootage <= 0) {
            throw new IllegalArgumentException("Square footage must be positive");
        }
        this.id            = id;
        this.unitNumber    = unitNumber;
        this.bedrooms      = bedrooms;
        this.bathrooms     = bathrooms;
        this.squareFootage = squareFootage;
        this.monthlyRent   = monthlyRent;
        this.status        = status;
    }

    /** Updates the asking rent for this unit. */
    public void updateRent(MonthlyRent newRent) {
        this.monthlyRent = newRent;
    }

    /** Marks the unit as occupied when a lease begins. */
    public void occupy() {
        if (status != UnitStatus.AVAILABLE && status != UnitStatus.RESERVED) {
            throw new IllegalStateException(
                    "Unit %s cannot be occupied from status %s".formatted(unitNumber, status));
        }
        this.status = UnitStatus.OCCUPIED;
    }

    /** Marks the unit as available again after a lease ends. */
    public void vacate() {
        if (status != UnitStatus.OCCUPIED) {
            throw new IllegalStateException(
                    "Unit %s cannot be vacated from status %s".formatted(unitNumber, status));
        }
        this.status = UnitStatus.AVAILABLE;
    }

    /** Puts the unit into maintenance mode. */
    public void startMaintenance() {
        if (status == UnitStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot perform maintenance on an occupied unit");
        }
        this.status = UnitStatus.UNDER_MAINTENANCE;
    }

    /** Completes maintenance and returns the unit to available. */
    public void completeMaintenance() {
        if (status != UnitStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException("Unit is not under maintenance");
        }
        this.status = UnitStatus.AVAILABLE;
    }

    public void reserve() {
        if (status != UnitStatus.AVAILABLE) {
            throw new IllegalStateException("Unit %s is not available for reservation".formatted(unitNumber));
        }
        this.status = UnitStatus.RESERVED;
    }

    /** Blocks the unit for any non-lease reason (maintenance, renovation, etc.). */
    public void markUnavailable() {
        if (status == UnitStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot block unit %s — it is currently OCCUPIED".formatted(unitNumber));
        }
        this.status = UnitStatus.UNAVAILABLE;
    }

    /** Returns a previously blocked unit to available. */
    public void markAvailable() {
        if (status != UnitStatus.UNAVAILABLE) {
            throw new IllegalStateException(
                    "Unit %s is not UNAVAILABLE (current: %s)".formatted(unitNumber, status));
        }
        this.status = UnitStatus.AVAILABLE;
    }
}
