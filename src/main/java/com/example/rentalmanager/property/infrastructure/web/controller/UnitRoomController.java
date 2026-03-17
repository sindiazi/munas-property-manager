package com.example.rentalmanager.property.infrastructure.web.controller;

import com.example.rentalmanager.property.application.dto.command.AddRoomImageCommand;
import com.example.rentalmanager.property.application.dto.command.CreateUnitRoomCommand;
import com.example.rentalmanager.property.application.dto.command.UpdateRoomImageCommand;
import com.example.rentalmanager.property.application.dto.command.UpdateUnitRoomCommand;
import com.example.rentalmanager.property.application.dto.response.UnitGalleryResponse;
import com.example.rentalmanager.property.application.dto.response.UnitRoomImageResponse;
import com.example.rentalmanager.property.application.dto.response.UnitRoomResponse;
import com.example.rentalmanager.property.application.port.input.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Unit Rooms", description = "Room gallery management for property units")
@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitRoomController {

    private final GetUnitRoomsUseCase   getUnitRoomsUseCase;
    private final CreateUnitRoomUseCase createUnitRoomUseCase;
    private final UpdateUnitRoomUseCase updateUnitRoomUseCase;
    private final DeleteUnitRoomUseCase deleteUnitRoomUseCase;
    private final AddRoomImageUseCase   addRoomImageUseCase;
    private final UpdateRoomImageUseCase updateRoomImageUseCase;
    private final DeleteRoomImageUseCase deleteRoomImageUseCase;

    @GetMapping("/{unitId}/rooms")
    public Mono<UnitGalleryResponse> getRooms(@PathVariable UUID unitId) {
        return getUnitRoomsUseCase.getRooms(unitId);
    }

    @PostMapping("/{unitId}/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UnitRoomResponse> createRoom(
            @PathVariable UUID unitId,
            @Valid @RequestBody CreateUnitRoomCommand body) {
        var cmd = new CreateUnitRoomCommand(unitId, body.type(), body.label(), body.displayOrder());
        return createUnitRoomUseCase.createRoom(cmd);
    }

    @PutMapping("/{unitId}/rooms/{roomId}")
    public Mono<UnitRoomResponse> updateRoom(
            @PathVariable UUID unitId,
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateUnitRoomCommand body) {
        var cmd = new UpdateUnitRoomCommand(unitId, roomId, body.type(), body.label(), body.displayOrder());
        return updateUnitRoomUseCase.updateRoom(cmd);
    }

    @DeleteMapping("/{unitId}/rooms/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteRoom(
            @PathVariable UUID unitId,
            @PathVariable UUID roomId) {
        return deleteUnitRoomUseCase.deleteRoom(unitId, roomId);
    }

    @PostMapping("/{unitId}/rooms/{roomId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UnitRoomImageResponse> addImage(
            @PathVariable UUID unitId,
            @PathVariable UUID roomId,
            @Valid @RequestBody AddRoomImageCommand body) {
        var cmd = new AddRoomImageCommand(unitId, roomId, body.url(), body.displayOrder(), body.caption());
        return addRoomImageUseCase.addImage(cmd);
    }

    @PutMapping("/{unitId}/rooms/{roomId}/images/{imageId}")
    public Mono<UnitRoomImageResponse> updateImage(
            @PathVariable UUID unitId,
            @PathVariable UUID roomId,
            @PathVariable UUID imageId,
            @Valid @RequestBody UpdateRoomImageCommand body) {
        var cmd = new UpdateRoomImageCommand(unitId, roomId, imageId, body.url(), body.displayOrder(), body.caption());
        return updateRoomImageUseCase.updateImage(cmd);
    }

    @DeleteMapping("/{unitId}/rooms/{roomId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteImage(
            @PathVariable UUID unitId,
            @PathVariable UUID roomId,
            @PathVariable UUID imageId) {
        return deleteRoomImageUseCase.deleteImage(roomId, imageId);
    }
}
