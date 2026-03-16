package com.example.rentalmanager.user.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordCommand(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8) String newPassword
) {}
