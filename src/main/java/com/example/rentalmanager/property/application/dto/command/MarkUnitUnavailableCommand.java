package com.example.rentalmanager.property.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/** Command to block a unit from being leased. */
public record MarkUnitUnavailableCommand(
        @NotNull UUID unitId,
        @NotBlank String reason,
        @NotNull LocalDate startDate,
        LocalDate endDate   // optional — null means open-ended
) {}
