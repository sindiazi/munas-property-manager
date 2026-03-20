package com.example.rentalmanager.user.infrastructure.persistence.repository;

import com.example.rentalmanager.user.infrastructure.persistence.entity.UserByUsernameEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface UserByUsernameRepository extends ReactiveCassandraRepository<UserByUsernameEntity, String> {
}
