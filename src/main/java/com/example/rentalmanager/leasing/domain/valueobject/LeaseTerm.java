package com.example.rentalmanager.leasing.domain.valueobject;

import java.time.LocalDate;
import java.time.Period;

/**
 * Immutable Value Object representing the time span of a lease.
 */
public record LeaseTerm(LocalDate startDate, LocalDate endDate) {

    public LeaseTerm {
        if (startDate == null) throw new IllegalArgumentException("startDate must not be null");
        if (endDate   == null) throw new IllegalArgumentException("endDate must not be null");
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
    }

    public int durationInMonths() {
        return Period.between(startDate, endDate).getMonths()
                + Period.between(startDate, endDate).getYears() * 12;
    }

    public boolean isActive(LocalDate asOf) {
        return !asOf.isBefore(startDate) && !asOf.isAfter(endDate);
    }

    public boolean isExpired(LocalDate asOf) {
        return asOf.isAfter(endDate);
    }
}
