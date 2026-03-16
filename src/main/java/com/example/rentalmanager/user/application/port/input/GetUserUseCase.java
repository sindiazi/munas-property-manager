package com.example.rentalmanager.user.application.port.input;

import com.example.rentalmanager.user.application.dto.response.UserResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetUserUseCase {
    Mono<UserResponse> getById(UUID id);
    Mono<UserResponse> getByUsername(String username);
    Flux<UserResponse> getAll();
}
