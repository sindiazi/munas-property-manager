package com.example.rentalmanager.leasing.application.dto.response;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Read-model DTO for a lease. */
public record LeaseResponse(
        UUID id,
        UUID tenantId,
        UUID propertyId,
        UUID unitId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyRent,
        BigDecimal securityDeposit,
        LeaseStatus status,
        String terminationReason,
        Instant createdAt
) {}
