package com.example.rentalmanager.maintenance.infrastructure.persistence.repository;

import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceIssueTemplateEntity;
import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceIssueTemplatePK;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MaintenanceIssueTemplateRepository
        extends ReactiveCassandraRepository<MaintenanceIssueTemplateEntity, MaintenanceIssueTemplatePK> {

    /** Fetches all issue templates for a given category (single partition read). */
    Flux<MaintenanceIssueTemplateEntity> findByKeyCategoryId(String categoryId);

    /** Deletes all issue templates for a given category (single partition delete). */
    Mono<Void> deleteByKeyCategoryId(String categoryId);
}
