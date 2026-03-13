package com.example.rentalmanager.maintenance.domain.service;

import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceRequest;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;

import java.util.List;

/** Domain Service for maintenance-related business rules. */
public class MaintenanceDomainService {

    /**
     * Determines whether a unit has active (open or in-progress) maintenance work,
     * which may prevent it from being made available for rent.
     */
    public boolean hasActiveMaintenanceWork(List<MaintenanceRequest> requests) {
        return requests.stream()
                .anyMatch(r -> r.getStatus() == MaintenanceStatus.OPEN
                        || r.getStatus() == MaintenanceStatus.ASSIGNED
                        || r.getStatus() == MaintenanceStatus.IN_PROGRESS);
    }

    /**
     * Finds the highest-priority open request for triage purposes.
     */
    public java.util.Optional<MaintenanceRequest> findHighestPriorityOpen(List<MaintenanceRequest> requests) {
        return requests.stream()
                .filter(r -> r.getStatus() == MaintenanceStatus.OPEN)
                .max(java.util.Comparator.comparingInt(r -> r.getPriority().ordinal()));
    }

    /**
     * Checks if an EMERGENCY request is open — triggers immediate escalation.
     */
    public boolean hasOpenEmergency(List<MaintenanceRequest> requests) {
        return requests.stream()
                .anyMatch(r -> r.getPriority() == MaintenancePriority.EMERGENCY
                        && r.getStatus() == MaintenanceStatus.OPEN);
    }
}
