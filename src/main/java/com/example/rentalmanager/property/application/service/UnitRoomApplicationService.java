package com.example.rentalmanager.property.application.service;

import com.example.rentalmanager.property.application.dto.command.AddRoomImageCommand;
import com.example.rentalmanager.property.application.dto.command.CreateUnitRoomCommand;
import com.example.rentalmanager.property.application.dto.command.UpdateRoomImageCommand;
import com.example.rentalmanager.property.application.dto.command.UpdateUnitRoomCommand;
import com.example.rentalmanager.property.application.dto.response.UnitGalleryResponse;
import com.example.rentalmanager.property.application.dto.response.UnitRoomImageResponse;
import com.example.rentalmanager.property.application.dto.response.UnitRoomResponse;
import com.example.rentalmanager.property.application.port.input.*;
import com.example.rentalmanager.property.application.port.output.UnitPersistencePort;
import com.example.rentalmanager.property.application.port.output.UnitRoomImagePersistencePort;
import com.example.rentalmanager.property.application.port.output.UnitRoomPersistencePort;
import com.example.rentalmanager.property.domain.aggregate.UnitRoom;
import com.example.rentalmanager.property.domain.aggregate.UnitRoomImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitRoomApplicationService
        implements GetUnitRoomsUseCase, CreateUnitRoomUseCase, UpdateUnitRoomUseCase,
                   DeleteUnitRoomUseCase, AddRoomImageUseCase, UpdateRoomImageUseCase,
                   DeleteRoomImageUseCase {

    private final UnitRoomPersistencePort      roomPort;
    private final UnitRoomImagePersistencePort imagePort;
    private final UnitPersistencePort          unitPort;

    // ── GetUnitRoomsUseCase ────────────────────────────────────────────────

    @Override
    public Mono<UnitGalleryResponse> getRooms(UUID unitId) {
        return roomPort.findByUnitId(unitId)
                .flatMap(room -> imagePort.findByRoomId(room.getId())
                        .collectList()
                        .map(images -> toRoomResponse(room, images)))
                .collectList()
                .map(rooms -> {
                    rooms.sort(Comparator.comparingInt(UnitRoomResponse::displayOrder));
                    return new UnitGalleryResponse(unitId, rooms);
                });
    }

    // ── CreateUnitRoomUseCase ──────────────────────────────────────────────

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<UnitRoomResponse> createRoom(CreateUnitRoomCommand cmd) {
        return unitPort.findById(cmd.unitId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unit not found: " + cmd.unitId())))
                .flatMap(unit -> resolveRoomDisplayOrder(cmd.unitId(), cmd.displayOrder()))
                .flatMap(order -> {
                    var room = UnitRoom.create(cmd.unitId(), cmd.type(), cmd.label(), order);
                    return roomPort.save(room);
                })
                .map(room -> toRoomResponse(room, List.of()));
    }

    // ── UpdateUnitRoomUseCase ──────────────────────────────────────────────

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<UnitRoomResponse> updateRoom(UpdateUnitRoomCommand cmd) {
        return roomPort.findById(cmd.roomId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Room not found: " + cmd.roomId())))
                .flatMap(existing -> {
                    if (!existing.getUnitId().equals(cmd.unitId())) {
                        return Mono.error(new IllegalArgumentException(
                                "Room " + cmd.roomId() + " does not belong to unit " + cmd.unitId()));
                    }
                    var updated = new UnitRoom(
                            existing.getId(),
                            existing.getUnitId(),
                            cmd.type()         != null ? cmd.type()         : existing.getType(),
                            cmd.label()        != null ? cmd.label()        : existing.getLabel(),
                            cmd.displayOrder() != null ? cmd.displayOrder() : existing.getDisplayOrder()
                    );
                    return roomPort.save(updated);
                })
                .flatMap(room -> imagePort.findByRoomId(room.getId())
                        .collectList()
                        .map(images -> toRoomResponse(room, images)));
    }

    // ── DeleteUnitRoomUseCase ──────────────────────────────────────────────

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<Void> deleteRoom(UUID unitId, UUID roomId) {
        return roomPort.findById(roomId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Room not found: " + roomId)))
                .flatMap(room -> {
                    if (!room.getUnitId().equals(unitId)) {
                        return Mono.error(new IllegalArgumentException(
                                "Room " + roomId + " does not belong to unit " + unitId));
                    }
                    return imagePort.deleteByRoomId(roomId)
                            .then(roomPort.deleteById(roomId));
                });
    }

    // ── AddRoomImageUseCase ────────────────────────────────────────────────

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<UnitRoomImageResponse> addImage(AddRoomImageCommand cmd) {
        return roomPort.findById(cmd.roomId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Room not found: " + cmd.roomId())))
                .flatMap(room -> {
                    if (!room.getUnitId().equals(cmd.unitId())) {
                        return Mono.error(new IllegalArgumentException(
                                "Room " + cmd.roomId() + " does not belong to unit " + cmd.unitId()));
                    }
                    return resolveImageDisplayOrder(cmd.roomId(), cmd.displayOrder());
                })
                .flatMap(order -> {
                    var image = UnitRoomImage.create(cmd.roomId(), cmd.url(), order, cmd.caption());
                    return imagePort.save(image);
                })
                .map(this::toImageResponse);
    }

    // ── UpdateRoomImageUseCase ─────────────────────────────────────────────

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<UnitRoomImageResponse> updateImage(UpdateRoomImageCommand cmd) {
        return imagePort.findById(cmd.imageId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Image not found: " + cmd.imageId())))
                .flatMap(existing -> {
                    if (!existing.getRoomId().equals(cmd.roomId())) {
                        return Mono.error(new IllegalArgumentException(
                                "Image " + cmd.imageId() + " does not belong to room " + cmd.roomId()));
                    }
                    var updated = new UnitRoomImage(
                            existing.getId(),
                            existing.getRoomId(),
                            cmd.url()          != null ? cmd.url()          : existing.getUrl(),
                            cmd.displayOrder() != null ? cmd.displayOrder() : existing.getDisplayOrder(),
                            cmd.caption()      != null ? cmd.caption()      : existing.getCaption(),
                            existing.getUploadedAt()
                    );
                    return imagePort.save(updated);
                })
                .map(this::toImageResponse);
    }

    // ── DeleteRoomImageUseCase ─────────────────────────────────────────────

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<Void> deleteImage(UUID roomId, UUID imageId) {
        return imagePort.findById(imageId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Image not found: " + imageId)))
                .flatMap(image -> {
                    if (!image.getRoomId().equals(roomId)) {
                        return Mono.error(new IllegalArgumentException(
                                "Image " + imageId + " does not belong to room " + roomId));
                    }
                    return imagePort.deleteById(imageId);
                });
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Mono<Integer> resolveRoomDisplayOrder(UUID unitId, Integer provided) {
        if (provided != null) {
            return Mono.just(provided);
        }
        return roomPort.countByUnitId(unitId);
    }

    private Mono<Integer> resolveImageDisplayOrder(UUID roomId, Integer provided) {
        if (provided != null) {
            return Mono.just(provided);
        }
        return imagePort.countByRoomId(roomId);
    }

    private UnitRoomResponse toRoomResponse(UnitRoom room, List<UnitRoomImage> images) {
        var imageResponses = images.stream()
                .sorted(Comparator.comparingInt(UnitRoomImage::getDisplayOrder))
                .map(this::toImageResponse)
                .toList();
        return new UnitRoomResponse(room.getId(), room.getType(), room.getLabel(),
                room.getDisplayOrder(), imageResponses);
    }

    private UnitRoomImageResponse toImageResponse(UnitRoomImage image) {
        return new UnitRoomImageResponse(image.getId(), image.getUrl(),
                image.getDisplayOrder(), image.getCaption(), image.getUploadedAt());
    }
}
