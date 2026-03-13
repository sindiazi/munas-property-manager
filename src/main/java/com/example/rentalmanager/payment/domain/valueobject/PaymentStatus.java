package com.example.rentalmanager.payment.domain.valueobject;

/** Lifecycle status of a payment record. */
public enum PaymentStatus {
    PENDING,
    PAID,
    OVERDUE,
    PARTIALLY_PAID,
    CANCELLED
}
