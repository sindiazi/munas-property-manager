package com.example.rentalmanager.user.infrastructure.persistence.repository;

import com.example.rentalmanager.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserCassandraRepository extends ReactiveCassandraRepository<UserEntity, UUID> {

}
