package com.example.rentalmanager.property.application.dto.command;

import java.util.UUID;

public record UpdateRoomImageCommand(
        UUID unitId,
        UUID roomId,
        UUID imageId,
        String url,
        Integer displayOrder,
        String caption
) {}
