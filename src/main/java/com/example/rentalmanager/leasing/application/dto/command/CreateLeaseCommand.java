package com.example.rentalmanager.leasing.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Command to draft a new lease agreement. */
public record CreateLeaseCommand(

        @NotNull UUID tenantId,
        @NotNull UUID propertyId,
        @NotNull UUID unitId,

        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,

        @NotNull @Positive BigDecimal monthlyRent,
        @NotNull @Positive BigDecimal securityDeposit
) {}
