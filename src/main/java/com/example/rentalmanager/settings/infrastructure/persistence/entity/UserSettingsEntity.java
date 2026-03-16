package com.example.rentalmanager.settings.infrastructure.persistence.entity;

import com.example.rentalmanager.settings.domain.valueobject.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_settings")
public class UserSettingsEntity {

    @PrimaryKey
    private UUID userId;
    private String currency;
    private Theme theme;
    private String timezone;
    private Instant updatedAt;
}
