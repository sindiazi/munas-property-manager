package com.example.rentalmanager.tenant.infrastructure.persistence.mapper;

import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.ContactInfo;
import com.example.rentalmanager.tenant.domain.valueobject.PersonalInfo;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import com.example.rentalmanager.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.stereotype.Component;

/** Anti-corruption mapper between {@link Tenant} domain object and R2DBC entity. */
@Component
public class TenantPersistenceMapper {

    public TenantJpaEntity toEntity(Tenant tenant) {
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
                .build();
    }

    public Tenant toDomain(TenantJpaEntity e) {
        return new Tenant(
                TenantId.of(e.getId()),
                new PersonalInfo(e.getFirstName(), e.getLastName(), e.getNationalId()),
                new ContactInfo(e.getEmail(), e.getPhoneNumber()),
                e.getCreditScore(), e.getStatus(), e.getRegisteredAt());
    }
}
