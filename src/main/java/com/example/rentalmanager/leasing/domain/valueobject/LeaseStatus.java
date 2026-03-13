package com.example.rentalmanager.leasing.domain.valueobject;

/** Lifecycle status of a lease agreement. */
public enum LeaseStatus {
    /** Initial state — lease created but not yet signed. */
    DRAFT,
    /** All parties have signed; lease is live. */
    ACTIVE,
    /** Lease has reached its natural end date. */
    EXPIRED,
    /** Lease was ended before the expiry date. */
    TERMINATED
}
