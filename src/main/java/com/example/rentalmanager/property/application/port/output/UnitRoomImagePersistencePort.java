package com.example.rentalmanager.property.application.port.output;

import com.example.rentalmanager.property.domain.aggregate.UnitRoomImage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UnitRoomImagePersistencePort {

    Mono<UnitRoomImage> save(UnitRoomImage image);

    Mono<UnitRoomImage> findById(UUID imageId);

    Flux<UnitRoomImage> findByRoomId(UUID roomId);

    Mono<Void> deleteById(UUID imageId);

    Mono<Void> deleteByRoomId(UUID roomId);

    Mono<Integer> countByRoomId(UUID roomId);
}
