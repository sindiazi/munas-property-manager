package com.example.rentalmanager.maintenance.domain.aggregate;

import com.example.rentalmanager.maintenance.domain.event.*;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.shared.domain.AggregateRoot;
import com.example.rentalmanager.shared.domain.DomainException;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root for maintenance reference data.
 * A category (e.g. "Plumbing") owns a list of issue templates that tenants
 * can select when submitting a maintenance request.
 */
@Getter
public class MaintenanceCategory extends AggregateRoot<String> {

    private final String id;
    private String name;
    private final List<MaintenanceIssueTemplate> issues;

    /** Reconstitution constructor — used by the persistence layer. */
    public MaintenanceCategory(String id, String name, List<MaintenanceIssueTemplate> issues) {
        this.id     = id;
        this.name   = name;
        this.issues = new ArrayList<>(issues);
    }

    @Override
    public String getId() {
        return id;
    }

    /** Returns an unmodifiable view of the issue templates. */
    public List<MaintenanceIssueTemplate> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    // ── Factory ────────────────────────────────────────────────────────────

    public static MaintenanceCategory create(String id, String name) {
        var category = new MaintenanceCategory(id, name, List.of());
        category.registerEvent(new MaintenanceCategoryCreatedEvent(
                UUID.randomUUID(), Instant.now(), id, name));
        return category;
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    public void update(String name) {
        this.name = name;
        registerEvent(new MaintenanceCategoryUpdatedEvent(
                UUID.randomUUID(), Instant.now(), id, name));
    }

    /**
     * Registers a deletion event so downstream listeners can react before the
     * aggregate is removed from persistence.
     */
    public void markDeleted() {
        registerEvent(new MaintenanceCategoryDeletedEvent(
                UUID.randomUUID(), Instant.now(), id));
    }

    public MaintenanceIssueTemplate addIssue(String issueId, String title,
                                             String description, MaintenancePriority priority) {
        if (issues.stream().anyMatch(i -> i.getId().equals(issueId))) {
            throw new DomainException(
                    "Issue '" + issueId + "' already exists in category '" + id + "'");
        }
        var issue = new MaintenanceIssueTemplate(issueId, id, title, description, priority);
        issues.add(issue);
        registerEvent(new MaintenanceIssueTemplateAddedEvent(
                UUID.randomUUID(), Instant.now(), id, issueId, title, priority));
        return issue;
    }

    public MaintenanceIssueTemplate updateIssue(String issueId, String title,
                                                String description, MaintenancePriority priority) {
        var existing = findIssueOrThrow(issueId);
        var updated = new MaintenanceIssueTemplate(
                issueId, id,
                title       != null ? title       : existing.getTitle(),
                description != null ? description : existing.getDescription(),
                priority    != null ? priority    : existing.getPriority());
        issues.replaceAll(i -> i.getId().equals(issueId) ? updated : i);
        registerEvent(new MaintenanceIssueTemplateUpdatedEvent(
                UUID.randomUUID(), Instant.now(), id, issueId));
        return updated;
    }

    public void removeIssue(String issueId) {
        findIssueOrThrow(issueId);
        issues.removeIf(i -> i.getId().equals(issueId));
        registerEvent(new MaintenanceIssueTemplateRemovedEvent(
                UUID.randomUUID(), Instant.now(), id, issueId));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private MaintenanceIssueTemplate findIssueOrThrow(String issueId) {
        return issues.stream()
                .filter(i -> i.getId().equals(issueId))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "Issue '" + issueId + "' not found in category '" + id + "'"));
    }
}
