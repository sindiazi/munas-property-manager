package com.example.rentalmanager.tenant.infrastructure.persistence.entity;

import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/** Spring Data R2DBC persistence entity for the {@code tenants} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tenants")
public class TenantJpaEntity {

    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String nationalId;
    private String email;
    private String phoneNumber;
    private int creditScore;
    private TenantStatus status;
    private Instant registeredAt;
}
