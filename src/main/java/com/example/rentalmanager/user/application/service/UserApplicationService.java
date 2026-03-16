package com.example.rentalmanager.user.application.service;

import com.example.rentalmanager.shared.domain.DomainException;
import com.example.rentalmanager.user.application.dto.command.ChangePasswordCommand;
import com.example.rentalmanager.user.application.dto.command.CreateUserCommand;
import com.example.rentalmanager.user.application.dto.command.UpdateUserCommand;
import com.example.rentalmanager.user.application.dto.response.UserResponse;
import com.example.rentalmanager.user.application.port.input.CreateUserUseCase;
import com.example.rentalmanager.user.application.port.input.GetUserUseCase;
import com.example.rentalmanager.user.application.port.input.UpdateUserUseCase;
import com.example.rentalmanager.user.application.port.output.UserPersistencePort;
import com.example.rentalmanager.user.domain.aggregate.User;
import com.example.rentalmanager.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class UserApplicationService implements CreateUserUseCase, GetUserUseCase, UpdateUserUseCase {

    private final UserPersistencePort persistencePort;
    private final PasswordEncoder     passwordEncoder;

    @Override
    public Mono<UserResponse> create(CreateUserCommand command) {
        return persistencePort.existsByUsername(command.username())
                .flatMap(exists -> {
                    if (exists) return Mono.error(new DomainException("Username already taken: " + command.username()));
                    return persistencePort.existsByEmail(command.email());
                })
                .flatMap(emailExists -> {
                    if (emailExists) return Mono.error(new DomainException("Email already registered: " + command.email()));
                    var hashed = passwordEncoder.encode(command.password());
                    var user   = User.create(command.username(), command.email(), hashed, command.role());
                    return persistencePort.save(user);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<UserResponse> getById(UUID id) {
        return persistencePort.findById(UserId.of(id))
                .switchIfEmpty(Mono.error(new DomainException("User not found: " + id)))
                .map(this::toResponse);
    }

    @Override
    public Mono<UserResponse> getByUsername(String username) {
        return persistencePort.findByUsername(username)
                .switchIfEmpty(Mono.error(new DomainException("User not found: " + username)))
                .map(this::toResponse);
    }

    @Override
    public Flux<UserResponse> getAll() {
        return persistencePort.findAll().map(this::toResponse);
    }

    @Override
    public Mono<UserResponse> update(UUID id, UpdateUserCommand command) {
        return persistencePort.findById(UserId.of(id))
                .switchIfEmpty(Mono.error(new DomainException("User not found: " + id)))
                .flatMap(user -> {
                    if (command.email()  != null) user.updateEmail(command.email());
                    if (command.role()   != null) user.changeRole(command.role());
                    if (command.active() != null) { if (command.active()) user.activate(); else user.deactivate(); }
                    return persistencePort.save(user);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> changePassword(UUID id, ChangePasswordCommand command) {
        return persistencePort.findById(UserId.of(id))
                .switchIfEmpty(Mono.error(new DomainException("User not found: " + id)))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
                        return Mono.error(new DomainException("Current password is incorrect"));
                    }
                    user.changePassword(passwordEncoder.encode(command.newPassword()));
                    return persistencePort.save(user);
                })
                .then();
    }

    @Override
    public Mono<Void> deactivate(UUID id) {
        return persistencePort.findById(UserId.of(id))
                .switchIfEmpty(Mono.error(new DomainException("User not found: " + id)))
                .flatMap(user -> { user.deactivate(); return persistencePort.save(user); })
                .then();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId().value(), user.getUsername(), user.getEmail(),
                user.getRole(), user.isActive(), user.getCreatedAt());
    }
}
