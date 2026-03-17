package com.example.rentalmanager.property.application.dto.command;

import com.example.rentalmanager.property.domain.valueobject.RoomType;

import java.util.UUID;

public record UpdateUnitRoomCommand(
        UUID unitId,
        UUID roomId,
        RoomType type,
        String label,
        Integer displayOrder
) {}
