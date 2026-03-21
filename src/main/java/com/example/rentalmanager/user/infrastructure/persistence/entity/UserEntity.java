package com.example.rentalmanager.user.infrastructure.persistence.entity;

import com.example.rentalmanager.user.domain.valueobject.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {

    @PrimaryKey
    private UUID id;
    private String username;
    private String email;
    @Column("password_hash")
    private String passwordHash;
    private UserRole role;
    private boolean active;
    @Column("created_at")
    private Instant createdAt;
}
