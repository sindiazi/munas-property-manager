package com.example.rentalmanager.leasing.infrastructure.persistence.repository;

import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryKey;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Read-model repository for the {@code unit_rental_history} projection table.
 * Querying by {@code unit_id} is a single-partition scan — no ALLOW FILTERING needed.
 */
@Repository
public interface UnitRentalHistoryRepository
        extends ReactiveCassandraRepository<UnitRentalHistoryEntity, UnitRentalHistoryKey> {

    Flux<UnitRentalHistoryEntity> findByKeyUnitId(UUID unitId);
}
