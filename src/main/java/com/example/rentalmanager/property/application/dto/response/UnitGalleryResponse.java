package com.example.rentalmanager.property.application.dto.response;

import java.util.List;
import java.util.UUID;

public record UnitGalleryResponse(
        UUID unitId,
        List<UnitRoomResponse> rooms
) {}
