package com.example.rentalmanager.property.application.dto.command;

import com.example.rentalmanager.property.domain.valueobject.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command DTO that drives the {@code CreateProperty} use case.
 * Carries raw, validated input from the delivery mechanism (HTTP, event, CLI).
 */
public record CreatePropertyCommand(

        @NotNull UUID ownerId,

        @NotBlank String name,

        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String zipCode,
        @NotBlank String country,

        @NotNull PropertyType type
) {}
