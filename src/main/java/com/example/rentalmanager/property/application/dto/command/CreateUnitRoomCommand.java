package com.example.rentalmanager.property.application.dto.command;

import com.example.rentalmanager.property.domain.valueobject.RoomType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUnitRoomCommand(
        UUID unitId,
        @NotNull RoomType type,
        String label,
        Integer displayOrder
) {}
