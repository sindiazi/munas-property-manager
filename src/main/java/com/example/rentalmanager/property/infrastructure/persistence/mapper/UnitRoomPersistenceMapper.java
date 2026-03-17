package com.example.rentalmanager.property.infrastructure.persistence.mapper;

import com.example.rentalmanager.property.domain.aggregate.UnitRoom;
import com.example.rentalmanager.property.domain.aggregate.UnitRoomImage;
import com.example.rentalmanager.property.domain.valueobject.RoomType;
import com.example.rentalmanager.property.infrastructure.persistence.entity.UnitRoomImageJpaEntity;
import com.example.rentalmanager.property.infrastructure.persistence.entity.UnitRoomJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UnitRoomPersistenceMapper {

    public UnitRoom toDomain(UnitRoomJpaEntity entity) {
        return new UnitRoom(
                entity.getId(),
                entity.getUnitId(),
                RoomType.valueOf(entity.getType()),
                entity.getLabel(),
                entity.getDisplayOrder()
        );
    }

    public UnitRoomJpaEntity toEntity(UnitRoom room) {
        return UnitRoomJpaEntity.builder()
                .id(room.getId())
                .unitId(room.getUnitId())
                .type(room.getType().name())
                .label(room.getLabel())
                .displayOrder(room.getDisplayOrder())
                .build();
    }

    public UnitRoomImage toDomain(UnitRoomImageJpaEntity entity) {
        return new UnitRoomImage(
                entity.getId(),
                entity.getRoomId(),
                entity.getUrl(),
                entity.getDisplayOrder(),
                entity.getCaption(),
                entity.getUploadedAt()
        );
    }

    public UnitRoomImageJpaEntity toEntity(UnitRoomImage image) {
        return UnitRoomImageJpaEntity.builder()
                .id(image.getId())
                .roomId(image.getRoomId())
                .url(image.getUrl())
                .displayOrder(image.getDisplayOrder())
                .caption(image.getCaption())
                .uploadedAt(image.getUploadedAt())
                .build();
    }
}
