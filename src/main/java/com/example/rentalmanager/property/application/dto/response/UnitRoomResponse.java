package com.example.rentalmanager.property.application.dto.response;

import com.example.rentalmanager.property.domain.valueobject.RoomType;

import java.util.List;
import java.util.UUID;

public record UnitRoomResponse(
        UUID id,
        RoomType type,
        String label,
        int displayOrder,
        List<UnitRoomImageResponse> images
) {}
