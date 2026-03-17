package com.example.rentalmanager.maintenance.domain.aggregate;

import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import lombok.Getter;

/**
 * Domain entity representing a predefined issue template within a {@link MaintenanceCategory}.
 * Immutable — the owning aggregate reconstructs instances on update.
 */
@Getter
public class MaintenanceIssueTemplate {

    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final MaintenancePriority priority;

    public MaintenanceIssueTemplate(String id, String categoryId,
                                    String title, String description,
                                    MaintenancePriority priority) {
        this.id          = id;
        this.categoryId  = categoryId;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
    }
}
