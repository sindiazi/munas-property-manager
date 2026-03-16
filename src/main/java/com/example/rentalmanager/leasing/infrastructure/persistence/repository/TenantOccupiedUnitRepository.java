package com.example.rentalmanager.leasing.infrastructure.persistence.repository;

import com.example.rentalmanager.leasing.infrastructure.persistence.entity.TenantOccupiedUnitEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Read-model repository for the {@code tenant_occupied_unit} projection table.
 * Keyed by tenant UUID; {@code findById} is a direct primary-key lookup.
 */
@Repository
public interface TenantOccupiedUnitRepository
        extends ReactiveCassandraRepository<TenantOccupiedUnitEntity, UUID> {
}
