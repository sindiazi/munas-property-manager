package com.example.rentalmanager.settings.infrastructure.web.controller;

import com.example.rentalmanager.settings.application.dto.command.UpdateSettingsCommand;
import com.example.rentalmanager.settings.application.dto.response.UserSettingsResponse;
import com.example.rentalmanager.settings.application.port.input.GetUserSettingsUseCase;
import com.example.rentalmanager.settings.application.port.input.UpdateUserSettingsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Settings", description = "Per-user settings management")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserSettingsController {

    private final GetUserSettingsUseCase    getSettingsUseCase;
    private final UpdateUserSettingsUseCase updateSettingsUseCase;

    @Operation(summary = "Get current user settings")
    @GetMapping
    public Mono<UserSettingsResponse> get(Authentication authentication) {
        UUID userId = (UUID) authentication.getDetails();
        return getSettingsUseCase.getByUserId(userId);
    }

    @Operation(summary = "Update current user settings")
    @PutMapping
    public Mono<UserSettingsResponse> update(Authentication authentication,
                                              @RequestBody UpdateSettingsCommand command) {
        UUID userId = (UUID) authentication.getDetails();
        return updateSettingsUseCase.update(userId, command);
    }
}
