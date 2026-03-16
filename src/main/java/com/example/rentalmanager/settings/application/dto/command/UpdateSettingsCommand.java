package com.example.rentalmanager.settings.application.dto.command;

import com.example.rentalmanager.settings.domain.valueobject.Theme;

public record UpdateSettingsCommand(String currency, Theme theme, String timezone) {}
