package com.example.rentalmanager.property.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("unit_rooms")
public class UnitRoomJpaEntity {

    @PrimaryKey
    private UUID id;

    @Indexed
    @Column("unit_id")
    private UUID unitId;

    private String type;

    private String label;

    @Column("display_order")
    private int displayOrder;
}
