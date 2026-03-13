package com.example.rentalmanager.leasing.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Command to terminate an active lease before its end date. */
public record TerminateLeaseCommand(
        @NotNull UUID leaseId,
        @NotBlank String reason
) {}
