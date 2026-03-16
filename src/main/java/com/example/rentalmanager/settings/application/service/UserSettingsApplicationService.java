package com.example.rentalmanager.settings.application.service;

import com.example.rentalmanager.settings.application.dto.command.UpdateSettingsCommand;
import com.example.rentalmanager.settings.application.dto.response.UserSettingsResponse;
import com.example.rentalmanager.settings.application.port.input.GetUserSettingsUseCase;
import com.example.rentalmanager.settings.application.port.input.UpdateUserSettingsUseCase;
import com.example.rentalmanager.settings.application.port.output.UserSettingsPersistencePort;
import com.example.rentalmanager.settings.domain.aggregate.UserSettings;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class UserSettingsApplicationService implements GetUserSettingsUseCase, UpdateUserSettingsUseCase {

    private final UserSettingsPersistencePort persistencePort;

    @Override
    public Mono<UserSettingsResponse> getByUserId(UUID userId) {
        return persistencePort.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> persistencePort.save(UserSettings.defaults(userId))))
                .map(this::toResponse);
    }

    @Override
    public Mono<UserSettingsResponse> update(UUID userId, UpdateSettingsCommand command) {
        return persistencePort.findByUserId(userId)
                .switchIfEmpty(Mono.just(UserSettings.defaults(userId)))
                .flatMap(settings -> {
                    settings.update(command.currency(), command.theme(), command.timezone());
                    return persistencePort.save(settings);
                })
                .map(this::toResponse);
    }

    private UserSettingsResponse toResponse(UserSettings s) {
        return new UserSettingsResponse(s.getUserId(), s.getCurrency(), s.getTheme(), s.getTimezone(), s.getUpdatedAt());
    }
}
