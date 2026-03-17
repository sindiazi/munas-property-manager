package com.example.rentalmanager.property.infrastructure.persistence.adapter;

import com.example.rentalmanager.property.application.port.output.UnitRoomPersistencePort;
import com.example.rentalmanager.property.domain.aggregate.UnitRoom;
import com.example.rentalmanager.property.infrastructure.persistence.mapper.UnitRoomPersistenceMapper;
import com.example.rentalmanager.property.infrastructure.persistence.repository.UnitRoomCassandraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnitRoomPersistenceAdapter implements UnitRoomPersistencePort {

    private final UnitRoomCassandraRepository repo;
    private final UnitRoomPersistenceMapper   mapper;

    @Override
    public Mono<UnitRoom> save(UnitRoom room) {
        return repo.save(mapper.toEntity(room)).map(mapper::toDomain);
    }

    @Override
    public Mono<UnitRoom> findById(UUID roomId) {
        return repo.findById(roomId).map(mapper::toDomain);
    }

    @Override
    public Flux<UnitRoom> findByUnitId(UUID unitId) {
        return repo.findByUnitId(unitId).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID roomId) {
        return repo.deleteById(roomId);
    }

    @Override
    public Mono<Integer> countByUnitId(UUID unitId) {
        return repo.countByUnitId(unitId).map(Long::intValue);
    }
}
