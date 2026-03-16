package com.example.rentalmanager.user.infrastructure.persistence.mapper;

import com.example.rentalmanager.user.domain.aggregate.User;
import com.example.rentalmanager.user.domain.valueobject.UserId;
import com.example.rentalmanager.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId().value())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toDomain(UserEntity e) {
        return new User(UserId.of(e.getId()), e.getUsername(), e.getEmail(),
                e.getPasswordHash(), e.getRole(), e.isActive(), e.getCreatedAt());
    }
}
