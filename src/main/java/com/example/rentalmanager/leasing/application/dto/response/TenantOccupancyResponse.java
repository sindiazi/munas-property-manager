package com.example.rentalmanager.leasing.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Read-model DTO: the unit a tenant is currently occupying. */
public record TenantOccupancyResponse(
        UUID tenantId,
        UUID unitId,
        UUID propertyId,
        UUID leaseId,
        BigDecimal monthlyRent,
        LocalDate leaseStart,
        LocalDate leaseEnd,
        Instant occupiedSince
) {}
