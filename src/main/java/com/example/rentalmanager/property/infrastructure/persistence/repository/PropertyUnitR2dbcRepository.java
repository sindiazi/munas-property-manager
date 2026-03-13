package com.example.rentalmanager.property.infrastructure.persistence.repository;

import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyUnitJpaEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Spring Data R2DBC repository for {@link PropertyUnitJpaEntity}. */
public interface PropertyUnitR2dbcRepository extends ReactiveCrudRepository<PropertyUnitJpaEntity, UUID> {

    Flux<PropertyUnitJpaEntity> findByPropertyId(UUID propertyId);

    Mono<Void> deleteByPropertyId(UUID propertyId);
}
