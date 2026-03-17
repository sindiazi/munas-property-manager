package com.example.rentalmanager.property.infrastructure.persistence.repository;

import com.example.rentalmanager.property.infrastructure.persistence.entity.UnitRoomJpaEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UnitRoomCassandraRepository extends ReactiveCassandraRepository<UnitRoomJpaEntity, UUID> {

    Flux<UnitRoomJpaEntity> findByUnitId(UUID unitId);

    Mono<Void> deleteByUnitId(UUID unitId);

    Mono<Long> countByUnitId(UUID unitId);
}
