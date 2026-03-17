package com.example.rentalmanager.property.domain.aggregate;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Domain entity representing a single image within a unit room gallery slide. */
@Getter
public class UnitRoomImage {

    private final UUID id;
    private final UUID roomId;
    private final String url;
    private final int displayOrder;
    private final String caption;
    private final Instant uploadedAt;

    /** Reconstitution constructor — used by the persistence layer. */
    public UnitRoomImage(UUID id, UUID roomId, String url, int displayOrder, String caption, Instant uploadedAt) {
        this.id           = id;
        this.roomId       = roomId;
        this.url          = url;
        this.displayOrder = displayOrder;
        this.caption      = caption;
        this.uploadedAt   = uploadedAt;
    }

    /** Factory method for creating a new room image. */
    public static UnitRoomImage create(UUID roomId, String url, int displayOrder, String caption) {
        return new UnitRoomImage(UUID.randomUUID(), roomId, url, displayOrder, caption, Instant.now());
    }
}
