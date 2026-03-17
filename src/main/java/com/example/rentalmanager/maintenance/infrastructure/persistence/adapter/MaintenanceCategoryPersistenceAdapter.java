package com.example.rentalmanager.maintenance.infrastructure.persistence.adapter;

import com.example.rentalmanager.maintenance.application.port.output.MaintenanceCategoryPersistencePort;
import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceCategory;
import com.example.rentalmanager.maintenance.infrastructure.persistence.mapper.MaintenanceCategoryPersistenceMapper;
import com.example.rentalmanager.maintenance.infrastructure.persistence.repository.MaintenanceCategoryRepository;
import com.example.rentalmanager.maintenance.infrastructure.persistence.repository.MaintenanceIssueTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MaintenanceCategoryPersistenceAdapter implements MaintenanceCategoryPersistencePort {

    private final MaintenanceCategoryRepository     categoryRepo;
    private final MaintenanceIssueTemplateRepository issueRepo;
    private final MaintenanceCategoryPersistenceMapper mapper;

    @Override
    public Mono<MaintenanceCategory> findById(String id) {
        return categoryRepo.findById(id)
                .flatMap(entity -> issueRepo.findByKeyCategoryId(id)
                        .collectList()
                        .map(issues -> mapper.toDomain(entity, issues)));
    }

    @Override
    public Flux<MaintenanceCategory> findAll() {
        return categoryRepo.findAll()
                .flatMap(entity -> issueRepo.findByKeyCategoryId(entity.getId())
                        .collectList()
                        .map(issues -> mapper.toDomain(entity, issues)));
    }

    @Override
    public Mono<MaintenanceCategory> save(MaintenanceCategory category) {
        var categoryEntity = mapper.categoryToEntity(category);
        var issueEntities  = category.getIssues().stream()
                .map(mapper::issueToEntity)
                .toList();

        return categoryRepo.save(categoryEntity)
                .then(issueRepo.deleteByKeyCategoryId(category.getId()))
                .thenMany(Flux.fromIterable(issueEntities).flatMap(issueRepo::save))
                .then(Mono.just(category));
    }

    @Override
    public Mono<Void> delete(String categoryId) {
        return issueRepo.deleteByKeyCategoryId(categoryId)
                .then(categoryRepo.deleteById(categoryId));
    }
}
