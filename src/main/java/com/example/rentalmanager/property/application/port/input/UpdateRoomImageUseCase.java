package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.UpdateRoomImageCommand;
import com.example.rentalmanager.property.application.dto.response.UnitRoomImageResponse;
import reactor.core.publisher.Mono;

public interface UpdateRoomImageUseCase {

    Mono<UnitRoomImageResponse> updateImage(UpdateRoomImageCommand cmd);
}
