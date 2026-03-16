package com.example.rentalmanager.property.infrastructure.persistence.repository;

import com.example.rentalmanager.property.infrastructure.persistence.entity.UnitUnavailabilityJpaEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/** Spring Data Cassandra repository for {@link UnitUnavailabilityJpaEntity}. */
public interface UnitUnavailabilityRepository
        extends ReactiveCassandraRepository<UnitUnavailabilityJpaEntity, UUID> {

    Flux<UnitUnavailabilityJpaEntity> findByUnitId(UUID unitId);
}
