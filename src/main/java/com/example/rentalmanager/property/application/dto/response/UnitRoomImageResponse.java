package com.example.rentalmanager.property.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UnitRoomImageResponse(
        UUID id,
        String url,
        int displayOrder,
        String caption,
        Instant uploadedAt
) {}
