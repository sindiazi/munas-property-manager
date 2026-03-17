package com.example.rentalmanager.maintenance.application.service;

import com.example.rentalmanager.maintenance.application.dto.command.*;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceCategoryResponse;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceIssueTemplateResponse;
import com.example.rentalmanager.maintenance.application.port.input.*;
import com.example.rentalmanager.maintenance.application.port.output.MaintenanceCategoryPersistencePort;
import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceCategory;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Application service for maintenance reference-data (categories and issue templates). */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceCategoryApplicationService
        implements GetMaintenanceCategoriesUseCase, CreateMaintenanceCategoryUseCase,
                   UpdateMaintenanceCategoryUseCase, DeleteMaintenanceCategoryUseCase,
                   AddIssueTemplateUseCase, UpdateIssueTemplateUseCase,
                   RemoveIssueTemplateUseCase {

    private final MaintenanceCategoryPersistencePort categoryPort;
    private final DomainEventPublisher               eventPublisher;

    // ── Reads (no role restriction — all authenticated users) ──────────────

    @Override
    public Flux<MaintenanceCategoryResponse> getAllCategories() {
        return categoryPort.findAll().map(this::toResponse);
    }

    @Override
    public Mono<MaintenanceCategoryResponse> getCategory(String categoryId) {
        return categoryPort.findById(categoryId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Maintenance category not found: " + categoryId)))
                .map(this::toResponse);
    }

    // ── Category writes ────────────────────────────────────────────────────

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<MaintenanceCategoryResponse> createCategory(CreateMaintenanceCategoryCommand cmd) {
        return categoryPort.findById(cmd.id())
                .flatMap(existing -> Mono.<MaintenanceCategoryResponse>error(
                        new IllegalArgumentException(
                                "Maintenance category already exists: " + cmd.id())))
                .switchIfEmpty(Mono.defer(() -> {
                    var category = MaintenanceCategory.create(cmd.id(), cmd.name());
                    return categoryPort.save(category)
                            .doOnSuccess(this::publishAndClear)
                            .map(this::toResponse);
                }));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<MaintenanceCategoryResponse> updateCategory(UpdateMaintenanceCategoryCommand cmd) {
        return categoryPort.findById(cmd.id())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Maintenance category not found: " + cmd.id())))
                .flatMap(category -> {
                    category.update(cmd.name());
                    return categoryPort.save(category);
                })
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<Void> deleteCategory(String categoryId) {
        return categoryPort.findById(categoryId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Maintenance category not found: " + categoryId)))
                .flatMap(category -> {
                    category.markDeleted();
                    return categoryPort.delete(categoryId)
                            .doOnSuccess(v -> publishAndClear(category));
                });
    }

    // ── Issue template writes ──────────────────────────────────────────────

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<MaintenanceCategoryResponse> addIssue(AddIssueTemplateCommand cmd) {
        return categoryPort.findById(cmd.categoryId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Maintenance category not found: " + cmd.categoryId())))
                .flatMap(category -> {
                    category.addIssue(cmd.id(), cmd.title(), cmd.description(), cmd.priority());
                    return categoryPort.save(category);
                })
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<MaintenanceCategoryResponse> updateIssue(UpdateIssueTemplateCommand cmd) {
        return categoryPort.findById(cmd.categoryId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Maintenance category not found: " + cmd.categoryId())))
                .flatMap(category -> {
                    category.updateIssue(cmd.id(), cmd.title(), cmd.description(), cmd.priority());
                    return categoryPort.save(category);
                })
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<Void> removeIssue(String categoryId, String issueId) {
        return categoryPort.findById(categoryId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Maintenance category not found: " + categoryId)))
                .flatMap(category -> {
                    category.removeIssue(issueId);
                    return categoryPort.save(category)
                            .doOnSuccess(this::publishAndClear)
                            .then();
                });
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void publishAndClear(MaintenanceCategory category) {
        category.getDomainEvents().forEach(eventPublisher::publish);
        category.clearDomainEvents();
    }

    private MaintenanceCategoryResponse toResponse(MaintenanceCategory category) {
        var issues = category.getIssues().stream()
                .map(i -> new MaintenanceIssueTemplateResponse(
                        i.getId(), i.getTitle(), i.getDescription(), i.getPriority()))
                .toList();
        return new MaintenanceCategoryResponse(category.getId(), category.getName(), issues);
    }
}
