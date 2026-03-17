package com.example.rentalmanager.property.domain.aggregate;

import com.example.rentalmanager.property.domain.valueobject.RoomType;
import lombok.Getter;

import java.util.UUID;

/** Domain entity representing a named room within a property unit, used for the gallery feature. */
@Getter
public class UnitRoom {

    private final UUID id;
    private final UUID unitId;
    private final RoomType type;
    private final String label;
    private final int displayOrder;

    /** Reconstitution constructor — used by the persistence layer. */
    public UnitRoom(UUID id, UUID unitId, RoomType type, String label, int displayOrder) {
        this.id           = id;
        this.unitId       = unitId;
        this.type         = type;
        this.label        = label;
        this.displayOrder = displayOrder;
    }

    /** Factory method for creating a new room. */
    public static UnitRoom create(UUID unitId, RoomType type, String label, int displayOrder) {
        return new UnitRoom(UUID.randomUUID(), unitId, type, label, displayOrder);
    }
}
