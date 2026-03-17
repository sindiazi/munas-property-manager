package com.example.rentalmanager.property.infrastructure.persistence.repository;

import com.example.rentalmanager.property.infrastructure.persistence.entity.UnitRoomImageJpaEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UnitRoomImageCassandraRepository extends ReactiveCassandraRepository<UnitRoomImageJpaEntity, UUID> {

    Flux<UnitRoomImageJpaEntity> findByRoomId(UUID roomId);

    Mono<Void> deleteByRoomId(UUID roomId);

    Mono<Long> countByRoomId(UUID roomId);
}
