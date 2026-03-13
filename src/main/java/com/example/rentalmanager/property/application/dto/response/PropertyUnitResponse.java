package com.example.rentalmanager.property.application.dto.response;

import com.example.rentalmanager.property.domain.valueobject.UnitStatus;

import java.math.BigDecimal;
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
        UnitStatus status
) {}
