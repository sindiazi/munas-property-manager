package com.example.rentalmanager.payment.domain.aggregate;

import com.example.rentalmanager.payment.domain.event.PaymentCreatedEvent;
import com.example.rentalmanager.payment.domain.event.PaymentOverdueEvent;
import com.example.rentalmanager.payment.domain.event.PaymentReceivedEvent;
import com.example.rentalmanager.payment.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: {@code Payment}
 *
 * <p>Represents a single payment obligation — a tenant owes a specific amount
 * by a specific date. Supports partial payments.
 */
@Getter
public class Payment extends AggregateRoot<PaymentId> {

    private final PaymentId   id;
    private final UUID        leaseId;
    private final UUID        tenantId;
    private final Money       amountDue;
    private Money             amountPaid;
    private final LocalDate   dueDate;
    private LocalDate         paidDate;
    private PaymentStatus     status;
    private final PaymentType type;
    private final Instant     createdAt;

    /** Reconstitution constructor. */
    public Payment(PaymentId id, UUID leaseId, UUID tenantId, Money amountDue, Money amountPaid,
                   LocalDate dueDate, LocalDate paidDate, PaymentStatus status,
                   PaymentType type, Instant createdAt) {
        this.id          = id;
        this.leaseId     = leaseId;
        this.tenantId    = tenantId;
        this.amountDue   = amountDue;
        this.amountPaid  = amountPaid;
        this.dueDate     = dueDate;
        this.paidDate    = paidDate;
        this.status      = status;
        this.type        = type;
        this.createdAt   = createdAt;
    }

    // ── Factory ────────────────────────────────────────────────────────────

    public static Payment create(UUID leaseId, UUID tenantId, Money amountDue,
                                 LocalDate dueDate, PaymentType type) {
        var id      = PaymentId.generate();
        var zeroPaid = Money.of(BigDecimal.ZERO, amountDue.currency().getCurrencyCode());
        var payment = new Payment(id, leaseId, tenantId, amountDue, zeroPaid,
                dueDate, null, PaymentStatus.PENDING, type, Instant.now());
        payment.registerEvent(new PaymentCreatedEvent(UUID.randomUUID(), Instant.now(),
                id, leaseId, tenantId, amountDue.amount(), type));
        return payment;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    /** Records a (possibly partial) payment. */
    public void receive(Money received, LocalDate paymentDate) {
        if (status == PaymentStatus.PAID || status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already " + status);
        }
        this.amountPaid = amountPaid.add(received);
        this.paidDate   = paymentDate;

        if (amountPaid.amount().compareTo(amountDue.amount()) >= 0) {
            this.status = PaymentStatus.PAID;
        } else {
            this.status = PaymentStatus.PARTIALLY_PAID;
        }
        registerEvent(new PaymentReceivedEvent(UUID.randomUUID(), Instant.now(),
                id, tenantId, received.amount()));
    }

    /** Marks the payment as overdue. */
    public void markOverdue() {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PARTIALLY_PAID) {
            throw new IllegalStateException("Cannot mark as overdue, current status: " + status);
        }
        this.status = PaymentStatus.OVERDUE;
        registerEvent(new PaymentOverdueEvent(UUID.randomUUID(), Instant.now(), id, tenantId, dueDate));
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    /** Outstanding balance remaining. */
    public BigDecimal outstandingBalance() {
        return amountDue.amount().subtract(amountPaid.amount());
    }
}
