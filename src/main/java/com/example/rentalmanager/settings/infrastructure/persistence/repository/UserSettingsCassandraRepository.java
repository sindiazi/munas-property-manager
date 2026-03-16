package com.example.rentalmanager.settings.infrastructure.persistence.repository;

import com.example.rentalmanager.settings.infrastructure.persistence.entity.UserSettingsEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import java.util.UUID;

public interface UserSettingsCassandraRepository extends ReactiveCassandraRepository<UserSettingsEntity, UUID> {}
