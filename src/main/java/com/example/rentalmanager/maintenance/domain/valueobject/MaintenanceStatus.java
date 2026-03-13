package com.example.rentalmanager.maintenance.domain.valueobject;

/** Lifecycle status of a maintenance request. */
public enum MaintenanceStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
