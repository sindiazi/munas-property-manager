package com.example.rentalmanager.leasing.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Command to draft a new lease agreement. */
public record CreateLeaseCommand(

        /**
         * Either a UUID string or a 9-digit SSN (no dashes).
         * The service resolves SSNs to the corresponding tenant UUID before persisting.
         */
        @NotBlank String tenantId,
        @NotNull UUID propertyId,
        @NotNull UUID unitId,

        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,

        @NotNull @Positive BigDecimal monthlyRent,
        @NotNull @Positive BigDecimal securityDeposit
) {}
