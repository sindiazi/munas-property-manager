package com.example.rentalmanager.property.application.dto.command;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/** Command to add a new rentable unit to an existing property. */
public record AddPropertyUnitCommand(

        @NotNull UUID propertyId,

        @NotBlank String unitNumber,

        @Min(0) int bedrooms,
        @Min(0) int bathrooms,

        @Positive double squareFootage,

        @NotNull @Positive BigDecimal monthlyRentAmount,
        @NotBlank String currencyCode
) {}
