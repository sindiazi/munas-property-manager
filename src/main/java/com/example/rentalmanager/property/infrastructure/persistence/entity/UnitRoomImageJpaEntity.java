package com.example.rentalmanager.property.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("unit_room_images")
public class UnitRoomImageJpaEntity {

    @PrimaryKey
    private UUID id;

    @Indexed
    @Column("room_id")
    private UUID roomId;

    private String url;

    @Column("display_order")
    private int displayOrder;

    private String caption;

    @Column("uploaded_at")
    private Instant uploadedAt;
}
