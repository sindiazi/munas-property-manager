package com.example.rentalmanager.user.application.port.input;

import com.example.rentalmanager.user.application.dto.command.ChangePasswordCommand;
import com.example.rentalmanager.user.application.dto.command.UpdateUserCommand;
import com.example.rentalmanager.user.application.dto.response.UserResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UpdateUserUseCase {
    Mono<UserResponse> update(UUID id, UpdateUserCommand command);
    Mono<Void> changePassword(UUID id, ChangePasswordCommand command);
    Mono<Void> deactivate(UUID id);
}
