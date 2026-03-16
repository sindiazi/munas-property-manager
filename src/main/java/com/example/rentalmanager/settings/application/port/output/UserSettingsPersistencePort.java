package com.example.rentalmanager.settings.application.port.output;

import com.example.rentalmanager.settings.domain.aggregate.UserSettings;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface UserSettingsPersistencePort {
    Mono<UserSettings> save(UserSettings settings);
    Mono<UserSettings> findByUserId(UUID userId);
}
