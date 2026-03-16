package com.example.rentalmanager.user.application.dto.response;

import com.example.rentalmanager.user.domain.valueobject.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        UserRole role,
        boolean active,
        Instant createdAt
) {}
