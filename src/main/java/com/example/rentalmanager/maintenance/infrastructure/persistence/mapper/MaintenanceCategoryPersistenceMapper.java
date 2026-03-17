package com.example.rentalmanager.maintenance.infrastructure.persistence.mapper;

import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceCategory;
import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceIssueTemplate;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceCategoryEntity;
import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceIssueTemplateEntity;
import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceIssueTemplatePK;
import org.springframework.stereotype.Component;

import java.util.List;

/** Manual mapper between domain objects and Cassandra entities for the maintenance category context. */
@Component
public class MaintenanceCategoryPersistenceMapper {

    public MaintenanceCategory toDomain(MaintenanceCategoryEntity entity,
                                        List<MaintenanceIssueTemplateEntity> issueEntities) {
        var issues = issueEntities.stream().map(this::issueToDomain).toList();
        return new MaintenanceCategory(entity.getId(), entity.getName(), issues);
    }

    public MaintenanceIssueTemplate issueToDomain(MaintenanceIssueTemplateEntity entity) {
        return new MaintenanceIssueTemplate(
                entity.getKey().getId(),
                entity.getKey().getCategoryId(),
                entity.getTitle(),
                entity.getDescription(),
                MaintenancePriority.valueOf(entity.getPriority()));
    }

    public MaintenanceCategoryEntity categoryToEntity(MaintenanceCategory domain) {
        return new MaintenanceCategoryEntity(domain.getId(), domain.getName());
    }

    public MaintenanceIssueTemplateEntity issueToEntity(MaintenanceIssueTemplate issue) {
        var pk = new MaintenanceIssueTemplatePK(issue.getCategoryId(), issue.getId());
        return new MaintenanceIssueTemplateEntity(
                pk, issue.getTitle(), issue.getDescription(), issue.getPriority().name());
    }
}
