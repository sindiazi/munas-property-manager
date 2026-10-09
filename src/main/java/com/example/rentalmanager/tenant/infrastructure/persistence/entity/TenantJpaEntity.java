package com.example.rentalmanager.tenant.infrastructure.persistence.entity;

import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
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
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    @Column("national_id")
    private String nationalId;
    @Indexed
    private String email;
    @Column("phone_number")
    private String phoneNumber;
    @Column("credit_score")
    private int creditScore;
    @Indexed
    private TenantStatus status;
    @Column("registered_at")
    private Instant registeredAt;
    /** AES-256/CBC encrypted National ID number: base64( IV[16] || ciphertext ). */
    @Column("national_id_no")
    private String nationalIdNo;
    /** HMAC-SHA-256 of normalised National ID digits — used for equality-lookup queries. */
    @Indexed
    @Column("national_id_no_hash")
    private String nationalIdNoHash;
}
