package com.example.rentalmanager.property.infrastructure.persistence.adapter;

import com.example.rentalmanager.property.application.port.output.UnitRoomImagePersistencePort;
import com.example.rentalmanager.property.domain.aggregate.UnitRoomImage;
import com.example.rentalmanager.property.infrastructure.persistence.mapper.UnitRoomPersistenceMapper;
import com.example.rentalmanager.property.infrastructure.persistence.repository.UnitRoomImageCassandraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnitRoomImagePersistenceAdapter implements UnitRoomImagePersistencePort {

    private final UnitRoomImageCassandraRepository repo;
    private final UnitRoomPersistenceMapper        mapper;

    @Override
    public Mono<UnitRoomImage> save(UnitRoomImage image) {
        return repo.save(mapper.toEntity(image)).map(mapper::toDomain);
    }

    @Override
    public Mono<UnitRoomImage> findById(UUID imageId) {
        return repo.findById(imageId).map(mapper::toDomain);
    }

    @Override
    public Flux<UnitRoomImage> findByRoomId(UUID roomId) {
        return repo.findByRoomId(roomId).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID imageId) {
        return repo.deleteById(imageId);
    }

    @Override
    public Mono<Void> deleteByRoomId(UUID roomId) {
        return repo.deleteByRoomId(roomId);
    }

    @Override
    public Mono<Integer> countByRoomId(UUID roomId) {
        return repo.countByRoomId(roomId).map(Long::intValue);
    }
}
