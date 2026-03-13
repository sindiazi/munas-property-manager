package com.example.rentalmanager.property.application.dto.response;

import com.example.rentalmanager.property.domain.valueobject.PropertyType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read-model DTO for a full property including its units. */
public record PropertyResponse(
        UUID id,
        UUID ownerId,
        String name,
        String street,
        String city,
        String state,
        String zipCode,
        String country,
        PropertyType type,
        List<PropertyUnitResponse> units,
        Instant createdAt
) {}
