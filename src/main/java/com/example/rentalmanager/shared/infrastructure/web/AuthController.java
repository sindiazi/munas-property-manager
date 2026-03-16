package com.example.rentalmanager.shared.infrastructure.web;

import com.example.rentalmanager.shared.infrastructure.security.JwtTokenProvider;
import com.example.rentalmanager.user.application.dto.response.UserResponse;
import com.example.rentalmanager.user.application.port.output.UserPersistencePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Tag(name = "Authentication", description = "JWT login and profile")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ReactiveAuthenticationManager authManager;
    private final JwtTokenProvider              tokenProvider;
    private final UserPersistencePort           userPersistencePort;

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String token, Instant expiresAt, UserResponse user) {}

    @Operation(summary = "Authenticate and receive JWT")
    @PostMapping("/login")
    public Mono<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.username(), request.password()))
                .flatMap(auth -> userPersistencePort.findByUsername(auth.getName()))
                .map(user -> {
                    String token = tokenProvider.generateToken(
                            user.getUsername(), user.getRole().name(), user.getId().value());
                    Instant expiresAt = Instant.now().plusMillis(86400000L);
                    var userResp = new UserResponse(user.getId().value(), user.getUsername(),
                            user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt());
                    return new TokenResponse(token, expiresAt, userResp);
                });
    }

    @Operation(summary = "Get current authenticated user")
    @GetMapping("/me")
    public Mono<UserResponse> me() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (String) ctx.getAuthentication().getPrincipal())
                .flatMap(userPersistencePort::findByUsername)
                .map(user -> new UserResponse(user.getId().value(), user.getUsername(),
                        user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt()));
    }
}
