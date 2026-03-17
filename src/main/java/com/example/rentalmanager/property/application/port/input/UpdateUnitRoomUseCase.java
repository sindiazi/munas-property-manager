package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.UpdateUnitRoomCommand;
import com.example.rentalmanager.property.application.dto.response.UnitRoomResponse;
import reactor.core.publisher.Mono;

public interface UpdateUnitRoomUseCase {

    Mono<UnitRoomResponse> updateRoom(UpdateUnitRoomCommand cmd);
}
