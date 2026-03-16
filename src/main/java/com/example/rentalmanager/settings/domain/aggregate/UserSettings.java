package com.example.rentalmanager.settings.domain.aggregate;

import com.example.rentalmanager.settings.domain.valueobject.Theme;
import com.example.rentalmanager.shared.domain.AggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserSettings extends AggregateRoot<UUID> {

    private final UUID userId;
    private String currency;
    private Theme  theme;
    private String timezone;
    private Instant updatedAt;

    public UserSettings(UUID userId, String currency, Theme theme, String timezone, Instant updatedAt) {
        this.userId    = userId;
        this.currency  = currency;
        this.theme     = theme;
        this.timezone  = timezone;
        this.updatedAt = updatedAt;
    }

    @Override
    public UUID getId() { return userId; }

    public static UserSettings defaults(UUID userId) {
        return new UserSettings(userId, "USD", Theme.LIGHT, "UTC", Instant.now());
    }

    public void update(String currency, Theme theme, String timezone) {
        if (currency != null) this.currency  = currency;
        if (theme    != null) this.theme     = theme;
        if (timezone != null) this.timezone  = timezone;
        this.updatedAt = Instant.now();
    }
}
