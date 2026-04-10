package com.example.rentalmanager.shared.infrastructure.config;

import com.example.rentalmanager.settings.application.port.output.UserSettingsPersistencePort;
import com.example.rentalmanager.settings.domain.aggregate.UserSettings;
import com.example.rentalmanager.settings.domain.valueobject.Theme;
import com.example.rentalmanager.user.application.dto.command.CreateUserCommand;
import com.example.rentalmanager.user.application.port.input.CreateUserUseCase;
import com.example.rentalmanager.user.application.port.output.UserPersistencePort;
import com.example.rentalmanager.user.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Seeds a default admin user on first startup if no users exist.
 *
 * Credentials are configurable via environment variables:
 *   ADMIN_USERNAME  (default: admin)
 *   ADMIN_EMAIL     (default: admin@example.com)
 *   ADMIN_PASSWORD  (default: Admin@1234)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserPersistencePort        userPersistencePort;
    private final CreateUserUseCase          createUserUseCase;
    private final UserSettingsPersistencePort settingsPersistencePort;
    private final Environment                env;

    @EventListener(ApplicationReadyEvent.class)
    public Mono<Void> seedDefaultAdmin() {
        String username = env.getProperty("ADMIN_USERNAME", "admin");
        String email    = env.getProperty("ADMIN_EMAIL",    "admin@example.com");
        String password = env.getProperty("ADMIN_PASSWORD", "Admin@1234");

        return userPersistencePort.existsByUsername(username)
                .flatMap(exists -> {
                    if (exists) {
                        log.debug("Admin user '{}' already exists — skipping seed.", username);
                        return Mono.empty();
                    }
                    log.info("Seeding default admin user '{}'", username);
                    return createUserUseCase.create(
                            new CreateUserCommand(username, email, password, UserRole.ADMIN))
                            .flatMap(user -> {
                                var settings = new UserSettings(
                                        user.id(), "KES", Theme.DARK, "Africa/Nairobi", Instant.now());
                                return settingsPersistencePort.save(settings);
                            });
                })
                .doOnError(e -> log.error("Failed to seed admin user: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }
}
