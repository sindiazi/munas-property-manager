package com.example.rentalmanager.property.application.port.input;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeleteRoomImageUseCase {

    Mono<Void> deleteImage(UUID roomId, UUID imageId);
}
