package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.response.UnitGalleryResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetUnitRoomsUseCase {

    Mono<UnitGalleryResponse> getRooms(UUID unitId);
}
