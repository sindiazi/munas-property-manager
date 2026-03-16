package com.example.rentalmanager.settings.application.port.input;

import com.example.rentalmanager.settings.application.dto.response.UserSettingsResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface GetUserSettingsUseCase {
    Mono<UserSettingsResponse> getByUserId(UUID userId);
}
