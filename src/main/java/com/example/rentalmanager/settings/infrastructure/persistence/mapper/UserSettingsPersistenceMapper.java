package com.example.rentalmanager.settings.infrastructure.persistence.mapper;

import com.example.rentalmanager.settings.domain.aggregate.UserSettings;
import com.example.rentalmanager.settings.infrastructure.persistence.entity.UserSettingsEntity;
import org.springframework.stereotype.Component;

@Component
public class UserSettingsPersistenceMapper {

    public UserSettingsEntity toEntity(UserSettings s) {
        return UserSettingsEntity.builder()
                .userId(s.getUserId())
                .currency(s.getCurrency())
                .theme(s.getTheme())
                .timezone(s.getTimezone())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    public UserSettings toDomain(UserSettingsEntity e) {
        return new UserSettings(e.getUserId(), e.getCurrency(), e.getTheme(), e.getTimezone(), e.getUpdatedAt());
    }
}
