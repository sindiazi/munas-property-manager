package com.example.rentalmanager.user.application.dto.command;

import com.example.rentalmanager.user.domain.valueobject.UserRole;
import jakarta.validation.constraints.Email;

public record UpdateUserCommand(
        @Email String email,
        UserRole role,
        Boolean active
) {}
