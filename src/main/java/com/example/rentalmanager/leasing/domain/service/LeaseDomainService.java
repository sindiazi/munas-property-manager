package com.example.rentalmanager.leasing.domain.service;

import com.example.rentalmanager.leasing.domain.aggregate.Lease;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;

import java.time.LocalDate;

/**
 * Domain Service for lease-related business rules that span beyond a single
 * {@code Lease} aggregate.
 */
public class LeaseDomainService {

    /**
     * Determines whether a unit is available for a new lease by checking
     * that there are no active or draft leases for it.
     */
    public boolean isUnitAvailableForLease(java.util.List<Lease> existingLeases) {
        return existingLeases.stream()
                .noneMatch(l -> l.getStatus() == LeaseStatus.ACTIVE
                        || l.getStatus() == LeaseStatus.DRAFT);
    }

    /**
     * Checks whether a lease has exceeded its natural end date and should be
     * expired.
     */
    public boolean shouldExpire(Lease lease, LocalDate today) {
        return lease.getStatus() == LeaseStatus.ACTIVE
                && lease.getTerm().isExpired(today);
    }
}
