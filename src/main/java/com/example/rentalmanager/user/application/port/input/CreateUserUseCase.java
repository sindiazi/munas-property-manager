package com.example.rentalmanager.user.application.port.input;

import com.example.rentalmanager.user.application.dto.command.CreateUserCommand;
import com.example.rentalmanager.user.application.dto.response.UserResponse;
import reactor.core.publisher.Mono;

public interface CreateUserUseCase {
    Mono<UserResponse> create(CreateUserCommand command);
}
