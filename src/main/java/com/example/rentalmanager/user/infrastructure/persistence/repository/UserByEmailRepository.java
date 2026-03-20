package com.example.rentalmanager.user.infrastructure.persistence.repository;

import com.example.rentalmanager.user.infrastructure.persistence.entity.UserByEmailEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface UserByEmailRepository extends ReactiveCassandraRepository<UserByEmailEntity, String> {
}
