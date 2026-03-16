package com.example.rentalmanager.user.domain.event;

import com.example.rentalmanager.shared.domain.DomainEvent;
import com.example.rentalmanager.user.domain.valueobject.UserId;
import com.example.rentalmanager.user.domain.valueobject.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(UUID eventId, Instant occurredOn,
                                   UserId userId, String username, UserRole role)
        implements DomainEvent {}
