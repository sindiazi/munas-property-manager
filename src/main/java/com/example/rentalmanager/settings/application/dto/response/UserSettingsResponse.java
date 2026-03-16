package com.example.rentalmanager.settings.application.dto.response;

import com.example.rentalmanager.settings.domain.valueobject.Theme;

import java.time.Instant;
import java.util.UUID;

public record UserSettingsResponse(UUID userId, String currency, Theme theme, String timezone, Instant updatedAt) {}
