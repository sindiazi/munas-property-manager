package com.example.rentalmanager.property.application.dto.response;

import com.example.rentalmanager.property.domain.valueobject.UnitStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Read-model DTO for a single property unit. */
public record PropertyUnitResponse(
        UUID id,
        String unitNumber,
        int bedrooms,
        int bathrooms,
        double squareFootage,
        BigDecimal monthlyRentAmount,
        String currencyCode,
        UnitStatus status,
        /** Most recent active (open-ended or future) unavailability record. Null when unit is not UNAVAILABLE. */
        UnitUnavailabilityResponse currentUnavailability,
        /** Full unavailability history for this unit, ordered newest-first. */
        List<UnitUnavailabilityResponse> unavailabilityHistory
) {}
