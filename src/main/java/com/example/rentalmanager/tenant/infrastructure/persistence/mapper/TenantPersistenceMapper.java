package com.example.rentalmanager.tenant.infrastructure.persistence.mapper;

import com.example.rentalmanager.shared.infrastructure.security.SsnEncryptionService;
import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.ContactInfo;
import com.example.rentalmanager.tenant.domain.valueobject.PersonalInfo;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import com.example.rentalmanager.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Anti-corruption mapper between {@link Tenant} domain object and Cassandra entity. */
@Component
@RequiredArgsConstructor
public class TenantPersistenceMapper {

    private final SsnEncryptionService ssnEncryptionService;

    public TenantJpaEntity toEntity(Tenant tenant) {
        String plainId          = tenant.getNationalIdNo();
        String encryptedId      = ssnEncryptionService.encrypt(plainId);
        String nationalIdNoHash = ssnEncryptionService.computeLookupHash(plainId);
        return TenantJpaEntity.builder()
                .id(tenant.getId().value())
                .firstName(tenant.getPersonalInfo().firstName())
                .lastName(tenant.getPersonalInfo().lastName())
                .nationalId(tenant.getPersonalInfo().nationalId())
                .email(tenant.getContactInfo().email())
                .phoneNumber(tenant.getContactInfo().phoneNumber())
                .creditScore(tenant.getCreditScore())
                .status(tenant.getStatus())
                .registeredAt(tenant.getRegisteredAt())
                .nationalIdNo(encryptedId)
                .nationalIdNoHash(nationalIdNoHash)
                .build();
    }

    public Tenant toDomain(TenantJpaEntity e) {
        String plainId = ssnEncryptionService.decrypt(e.getNationalIdNo());
        return new Tenant(
                TenantId.of(e.getId()),
                new PersonalInfo(e.getFirstName(), e.getLastName(), e.getNationalId()),
                new ContactInfo(e.getEmail(), e.getPhoneNumber()),
                e.getCreditScore(), e.getStatus(), e.getRegisteredAt(), plainId);
    }
}
