package com.example.rentalmanager.tenant.application.dto.response;

import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;

import java.time.Instant;
import java.util.UUID;

/** Read-model DTO for a tenant. */
public record TenantResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        int creditScore,
        TenantStatus status,
        Instant registeredAt
) {}
