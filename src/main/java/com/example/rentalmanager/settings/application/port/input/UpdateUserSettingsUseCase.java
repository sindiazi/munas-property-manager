package com.example.rentalmanager.settings.application.port.input;

import com.example.rentalmanager.settings.application.dto.command.UpdateSettingsCommand;
import com.example.rentalmanager.settings.application.dto.response.UserSettingsResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface UpdateUserSettingsUseCase {
    Mono<UserSettingsResponse> update(UUID userId, UpdateSettingsCommand command);
}
