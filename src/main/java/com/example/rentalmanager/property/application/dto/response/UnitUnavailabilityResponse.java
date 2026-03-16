package com.example.rentalmanager.property.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Read-model DTO for a single unit unavailability record. */
public record UnitUnavailabilityResponse(
        UUID      id,
        String    reason,
        LocalDate startDate,
        LocalDate endDate,    // null = open-ended
        Instant   createdAt
) {}
