package com.example.rentalmanager.property.domain.valueobject;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Immutable record representing a period during which a unit was unavailable.
 * An open-ended period (endDate == null) means the unit is still blocked.
 */
public record UnitUnavailability(
        UUID      id,
        UUID      unitId,
        String    reason,
        LocalDate startDate,
        LocalDate endDate,    // null = open-ended
        Instant   createdAt
) {}
