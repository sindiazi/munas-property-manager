package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.AddRoomImageCommand;
import com.example.rentalmanager.property.application.dto.response.UnitRoomImageResponse;
import reactor.core.publisher.Mono;

public interface AddRoomImageUseCase {

    Mono<UnitRoomImageResponse> addImage(AddRoomImageCommand cmd);
}
