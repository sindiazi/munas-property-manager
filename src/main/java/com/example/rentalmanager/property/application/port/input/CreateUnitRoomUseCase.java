package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.CreateUnitRoomCommand;
import com.example.rentalmanager.property.application.dto.response.UnitRoomResponse;
import reactor.core.publisher.Mono;

public interface CreateUnitRoomUseCase {

    Mono<UnitRoomResponse> createRoom(CreateUnitRoomCommand cmd);
}
