package com.example.rentalmanager.user.application.dto.command;

import com.example.rentalmanager.user.domain.valueobject.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserCommand(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull UserRole role
) {}
