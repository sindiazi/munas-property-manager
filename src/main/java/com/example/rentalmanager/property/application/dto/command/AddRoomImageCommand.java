package com.example.rentalmanager.property.application.dto.command;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AddRoomImageCommand(
        UUID unitId,
        UUID roomId,
        @NotBlank String url,
        Integer displayOrder,
        String caption
) {}
