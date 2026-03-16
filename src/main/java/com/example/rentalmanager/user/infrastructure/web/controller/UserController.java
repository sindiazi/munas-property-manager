package com.example.rentalmanager.user.infrastructure.web.controller;

import com.example.rentalmanager.user.application.dto.command.ChangePasswordCommand;
import com.example.rentalmanager.user.application.dto.command.CreateUserCommand;
import com.example.rentalmanager.user.application.dto.command.UpdateUserCommand;
import com.example.rentalmanager.user.application.dto.response.UserResponse;
import com.example.rentalmanager.user.application.port.input.CreateUserUseCase;
import com.example.rentalmanager.user.application.port.input.GetUserUseCase;
import com.example.rentalmanager.user.application.port.input.UpdateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Users", description = "User management (Admin only)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase    getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    @Operation(summary = "Create a new user (Admin only)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> create(@Valid @RequestBody CreateUserCommand command) {
        return createUserUseCase.create(command);
    }

    @Operation(summary = "List all users (Admin only)")
    @GetMapping
    public Flux<UserResponse> getAll() {
        return getUserUseCase.getAll();
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public Mono<UserResponse> getById(@PathVariable UUID id) {
        return getUserUseCase.getById(id);
    }

    @Operation(summary = "Update user (Admin only)")
    @PutMapping("/{id}")
    public Mono<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserCommand command) {
        return updateUserUseCase.update(id, command);
    }

    @Operation(summary = "Deactivate user (Admin only)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deactivate(@PathVariable UUID id) {
        return updateUserUseCase.deactivate(id);
    }

    @Operation(summary = "Change password")
    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> changePassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordCommand command) {
        return updateUserUseCase.changePassword(id, command);
    }
}
