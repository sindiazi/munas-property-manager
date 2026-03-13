package com.example.rentalmanager.property.application.dto.command;

import java.util.UUID;

/** Command to update mutable details of an existing property. */
public record UpdatePropertyCommand(
        UUID propertyId,
        String name,
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {}
