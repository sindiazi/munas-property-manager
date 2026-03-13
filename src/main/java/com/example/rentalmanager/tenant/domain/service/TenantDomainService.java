package com.example.rentalmanager.tenant.domain.service;

import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;

/**
 * Domain Service containing tenant-related business rules that span
 * beyond the single {@code Tenant} aggregate.
 */
public class TenantDomainService {

    private static final int MINIMUM_CREDIT_SCORE_FOR_LEASE = 620;

    /**
     * Determines whether a tenant is eligible to enter a new lease agreement.
     */
    public boolean isEligibleForLease(Tenant tenant) {
        return tenant.getStatus() == TenantStatus.ACTIVE
                && tenant.getCreditScore() >= MINIMUM_CREDIT_SCORE_FOR_LEASE;
    }

    /**
     * Calculates a rental risk level based on credit score.
     *
     * @return "LOW", "MEDIUM", or "HIGH"
     */
    public String assessRentalRisk(Tenant tenant) {
        return switch (tenant.getCreditScore()) {
            case int s when s >= 750 -> "LOW";
            case int s when s >= 650 -> "MEDIUM";
            default                  -> "HIGH";
        };
    }
}
