package com.example.rentalmanager.maintenance.infrastructure.web.controller;

import com.example.rentalmanager.maintenance.application.dto.command.*;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceCategoryResponse;
import com.example.rentalmanager.maintenance.application.port.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Maintenance Categories", description = "Reference data for maintenance request categorisation")
@RestController
@RequestMapping("/api/v1/maintenance/categories")
@RequiredArgsConstructor
public class MaintenanceCategoryController {

    private final GetMaintenanceCategoriesUseCase  getUseCase;
    private final CreateMaintenanceCategoryUseCase createUseCase;
    private final UpdateMaintenanceCategoryUseCase updateUseCase;
    private final DeleteMaintenanceCategoryUseCase deleteUseCase;
    private final AddIssueTemplateUseCase          addIssueUseCase;
    private final UpdateIssueTemplateUseCase       updateIssueUseCase;
    private final RemoveIssueTemplateUseCase       removeIssueUseCase;

    // ── Categories ─────────────────────────────────────────────────────────

    @Operation(summary = "List all maintenance categories with their issue templates")
    @GetMapping
    public Flux<MaintenanceCategoryResponse> getAllCategories() {
        return getUseCase.getAllCategories();
    }

    @Operation(summary = "Get a single maintenance category by id")
    @GetMapping("/{categoryId}")
    public Mono<MaintenanceCategoryResponse> getCategory(@PathVariable String categoryId) {
        return getUseCase.getCategory(categoryId);
    }

    @Operation(summary = "Create a new maintenance category")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MaintenanceCategoryResponse> createCategory(
            @Valid @RequestBody CreateMaintenanceCategoryCommand body) {
        return createUseCase.createCategory(body);
    }

    @Operation(summary = "Update a maintenance category's name")
    @PutMapping("/{categoryId}")
    public Mono<MaintenanceCategoryResponse> updateCategory(
            @PathVariable String categoryId,
            @Valid @RequestBody UpdateMaintenanceCategoryCommand body) {
        var cmd = new UpdateMaintenanceCategoryCommand(categoryId, body.name());
        return updateUseCase.updateCategory(cmd);
    }

    @Operation(summary = "Delete a maintenance category and all its issue templates")
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCategory(@PathVariable String categoryId) {
        return deleteUseCase.deleteCategory(categoryId);
    }

    // ── Issue templates ────────────────────────────────────────────────────

    @Operation(summary = "Add an issue template to a category")
    @PostMapping("/{categoryId}/issues")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MaintenanceCategoryResponse> addIssue(
            @PathVariable String categoryId,
            @Valid @RequestBody AddIssueTemplateCommand body) {
        var cmd = new AddIssueTemplateCommand(
                categoryId, body.id(), body.title(), body.description(), body.priority());
        return addIssueUseCase.addIssue(cmd);
    }

    @Operation(summary = "Update an issue template within a category")
    @PutMapping("/{categoryId}/issues/{issueId}")
    public Mono<MaintenanceCategoryResponse> updateIssue(
            @PathVariable String categoryId,
            @PathVariable String issueId,
            @RequestBody UpdateIssueTemplateCommand body) {
        var cmd = new UpdateIssueTemplateCommand(
                categoryId, issueId, body.title(), body.description(), body.priority());
        return updateIssueUseCase.updateIssue(cmd);
    }

    @Operation(summary = "Remove an issue template from a category")
    @DeleteMapping("/{categoryId}/issues/{issueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeIssue(
            @PathVariable String categoryId,
            @PathVariable String issueId) {
        return removeIssueUseCase.removeIssue(categoryId, issueId);
    }
}
