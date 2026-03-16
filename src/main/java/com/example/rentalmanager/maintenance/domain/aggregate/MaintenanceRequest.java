package com.example.rentalmanager.maintenance.domain.aggregate;

import com.example.rentalmanager.maintenance.domain.event.MaintenanceRequestCreatedEvent;
import com.example.rentalmanager.maintenance.domain.event.MaintenanceRequestStatusChangedEvent;
import com.example.rentalmanager.maintenance.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: {@code MaintenanceRequest}
 *
 * <p>Represents a request submitted by a tenant (or property manager) to fix or
 * improve something at a rental unit. Manages the full lifecycle from OPEN
 * through IN_PROGRESS to COMPLETED or CANCELLED.
 */
public class MaintenanceRequest extends AggregateRoot<RequestId> {

    private final RequestId          id;
    private final UUID               propertyId;
    private final UUID               unitId;
    private final UUID               tenantId;
    private WorkDescription          description;
    private MaintenancePriority      priority;
    private MaintenanceStatus        status;
    private final Instant            requestedAt;
    private Instant                  completedAt;

    @Override public RequestId         getId()           { return id; }
    public UUID                        getPropertyId()   { return propertyId; }
    public UUID                        getUnitId()       { return unitId; }
    public UUID                        getTenantId()     { return tenantId; }
    public WorkDescription             getDescription()  { return description; }
    public MaintenancePriority         getPriority()     { return priority; }
    public MaintenanceStatus           getStatus()       { return status; }
    public Instant                     getRequestedAt()  { return requestedAt; }
    public Instant                     getCompletedAt()  { return completedAt; }

    /** Reconstitution constructor. */
    public MaintenanceRequest(RequestId id, UUID propertyId, UUID unitId, UUID tenantId,
                              WorkDescription description, MaintenancePriority priority,
                              MaintenanceStatus status, Instant requestedAt, Instant completedAt) {
        this.id          = id;
        this.propertyId  = propertyId;
        this.unitId      = unitId;
        this.tenantId    = tenantId;
        this.description = description;
        this.priority    = priority;
        this.status      = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    // ── Factory ────────────────────────────────────────────────────────────

    public static MaintenanceRequest open(UUID propertyId, UUID unitId, UUID tenantId,
                                          WorkDescription description, MaintenancePriority priority) {
        var id      = RequestId.generate();
        var request = new MaintenanceRequest(id, propertyId, unitId, tenantId, description,
                priority, MaintenanceStatus.OPEN, Instant.now(), null);
        request.registerEvent(new MaintenanceRequestCreatedEvent(UUID.randomUUID(), Instant.now(),
                id, propertyId, unitId, tenantId, priority));
        return request;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    public void assign() {
        transition(MaintenanceStatus.ASSIGNED);
    }

    public void startWork() {
        if (status != MaintenanceStatus.ASSIGNED && status != MaintenanceStatus.OPEN) {
            throw new IllegalStateException("Work can only start from OPEN or ASSIGNED status");
        }
        transition(MaintenanceStatus.IN_PROGRESS);
    }

    public void complete(String resolutionNotes) {
        if (status != MaintenanceStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS requests can be completed");
        }
        this.description = description.withResolution(resolutionNotes);
        this.completedAt = Instant.now();
        transition(MaintenanceStatus.COMPLETED);
    }

    public void cancel() {
        if (status == MaintenanceStatus.COMPLETED) {
            throw new IllegalStateException("Completed requests cannot be cancelled");
        }
        transition(MaintenanceStatus.CANCELLED);
    }

    public void updatePriority(MaintenancePriority newPriority) {
        if (status == MaintenanceStatus.COMPLETED || status == MaintenanceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update priority of a closed request");
        }
        this.priority = newPriority;
    }

    private void transition(MaintenanceStatus newStatus) {
        var previous = this.status;
        this.status  = newStatus;
        registerEvent(new MaintenanceRequestStatusChangedEvent(UUID.randomUUID(), Instant.now(),
                id, previous, newStatus));
    }
}
