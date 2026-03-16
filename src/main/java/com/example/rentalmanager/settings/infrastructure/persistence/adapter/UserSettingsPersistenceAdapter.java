package com.example.rentalmanager.settings.infrastructure.persistence.adapter;

import com.example.rentalmanager.settings.application.port.output.UserSettingsPersistencePort;
import com.example.rentalmanager.settings.domain.aggregate.UserSettings;
import com.example.rentalmanager.settings.infrastructure.persistence.mapper.UserSettingsPersistenceMapper;
import com.example.rentalmanager.settings.infrastructure.persistence.repository.UserSettingsCassandraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserSettingsPersistenceAdapter implements UserSettingsPersistencePort {

    private final UserSettingsCassandraRepository repository;
    private final UserSettingsPersistenceMapper   mapper;

    @Override
    public Mono<UserSettings> save(UserSettings settings) {
        return repository.save(mapper.toEntity(settings)).map(mapper::toDomain);
    }

    @Override
    public Mono<UserSettings> findByUserId(UUID userId) {
        return repository.findById(userId).map(mapper::toDomain);
    }
}
