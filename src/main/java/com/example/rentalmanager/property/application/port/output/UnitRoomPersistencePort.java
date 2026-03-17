package com.example.rentalmanager.property.application.port.output;

import com.example.rentalmanager.property.domain.aggregate.UnitRoom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UnitRoomPersistencePort {

    Mono<UnitRoom> save(UnitRoom room);

    Mono<UnitRoom> findById(UUID roomId);

    Flux<UnitRoom> findByUnitId(UUID unitId);

    Mono<Void> deleteById(UUID roomId);

    Mono<Integer> countByUnitId(UUID unitId);
}
