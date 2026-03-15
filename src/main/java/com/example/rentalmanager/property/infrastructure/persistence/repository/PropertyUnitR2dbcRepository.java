package com.example.rentalmanager.property.infrastructure.persistence.repository;

import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyUnitJpaEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Spring Data Cassandra repository for {@link PropertyUnitJpaEntity}. */
public interface PropertyUnitR2dbcRepository extends ReactiveCassandraRepository<PropertyUnitJpaEntity, UUID> {

    Flux<PropertyUnitJpaEntity> findByPropertyId(UUID propertyId);

    Mono<Void> deleteByPropertyId(UUID propertyId);
}
