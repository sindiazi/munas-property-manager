package com.example.rentalmanager.maintenance.infrastructure.persistence.repository;

import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceCategoryEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceCategoryRepository
        extends ReactiveCassandraRepository<MaintenanceCategoryEntity, String> {
}
