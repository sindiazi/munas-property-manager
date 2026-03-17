package com.example.rentalmanager.property.application.port.input;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeleteUnitRoomUseCase {

    Mono<Void> deleteRoom(UUID unitId, UUID roomId);
}
