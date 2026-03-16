package com.example.rentalmanager.leasing.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Read-model DTO: a single lease entry in a unit's rental history. */
public record UnitRentalHistoryResponse(
        UUID unitId,
        UUID leaseId,
        UUID tenantId,
        UUID propertyId,
        BigDecimal monthlyRent,
        LocalDate leaseStart,
        LocalDate leaseEnd,
        String status
) {}
