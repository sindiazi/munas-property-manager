package com.example.rentalmanager.property.infrastructure.persistence.repository;

import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyJpaEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Spring Data Cassandra repository for {@link PropertyJpaEntity}.
 * Framework provides the implementation at runtime.
 */
public interface PropertyR2dbcRepository extends ReactiveCassandraRepository<PropertyJpaEntity, UUID> {

    Flux<PropertyJpaEntity> findByOwnerId(UUID ownerId);
}
