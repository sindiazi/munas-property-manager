package com.example.rentalmanager.property.domain.valueobject;

/** Lifecycle status of a rentable unit. */
public enum UnitStatus {
    /** Ready to be shown and rented. */
    AVAILABLE,
    /** Currently occupied by a tenant under an active lease. */
    OCCUPIED,
    /** Blocked by management: maintenance, renovation, or any other reason. */
    UNAVAILABLE,
    /** Temporarily unavailable due to ongoing maintenance work (legacy — maps to UNAVAILABLE in UI). */
    UNDER_MAINTENANCE,
    /** Reserved / application pending. */
    RESERVED
}
