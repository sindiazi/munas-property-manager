package com.example.rentalmanager.payment.domain.service;

import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Domain Service for payment-related business rules. */
public class PaymentDomainService {

    /**
     * Calculates the total outstanding balance across a list of payments
     * belonging to a lease.
     */
    public BigDecimal totalOutstanding(List<Payment> payments) {
        return payments.stream()
                .filter(p -> p.getStatus() != PaymentStatus.PAID
                        && p.getStatus() != PaymentStatus.CANCELLED)
                .map(Payment::outstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Identifies payments that are past due as of today.
     *
     * @param payments all payments to check
     * @param today    reference date
     * @return payments that should be marked overdue
     */
    public List<Payment> findOverduePayments(List<Payment> payments, LocalDate today) {
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING
                        || p.getStatus() == PaymentStatus.PARTIALLY_PAID)
                .filter(p -> p.getDueDate().isBefore(today))
                .toList();
    }
}
