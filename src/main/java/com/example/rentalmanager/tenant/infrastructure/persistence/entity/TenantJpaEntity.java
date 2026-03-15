package com.example.rentalmanager.tenant.infrastructure.persistence.entity;

import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/** Spring Data Cassandra persistence entity for the {@code tenants} table. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tenants")
public class TenantJpaEntity {

    @PrimaryKey
    private UUID id;
    private String firstName;
    private String lastName;
    private String nationalId;
    @Indexed
    private String email;
    private String phoneNumber;
    private int creditScore;
    @Indexed
    private TenantStatus status;
    private Instant registeredAt;
}