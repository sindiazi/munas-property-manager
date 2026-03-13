package com.example.rentalmanager.property.infrastructure.persistence.repository;

import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyJpaEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Spring Data R2DBC repository for {@link PropertyJpaEntity}.
 * Framework provides the implementation at runtime.
 */
public interface PropertyR2dbcRepository extends ReactiveCrudRepository<PropertyJpaEntity, UUID> {

    Flux<PropertyJpaEntity> findByOwnerId(UUID ownerId);
}
