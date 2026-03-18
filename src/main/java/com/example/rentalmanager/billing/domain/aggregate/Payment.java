package com.example.rentalmanager.billing.domain.aggregate;

import com.example.rentalmanager.billing.domain.event.PaymentRecordedEvent;
import com.example.rentalmanager.billing.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.AggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: {@code Payment} (transaction)
 *
 * <p>Represents an actual payment transaction recorded against an Invoice.
 */
public class Payment extends AggregateRoot<PaymentId> {

    private final PaymentId              id;
    private final UUID                   invoiceId;
    private final UUID                   tenantId;
    private final Money                  amount;
    private final PaymentMethod          method;
    private       PaymentTransactionStatus status;
    private       String                 reference;
    private final LocalDate              paymentDate;
    private final Instant                createdAt;

    @Override public PaymentId              getId()          { return id; }
    public UUID                             getInvoiceId()   { return invoiceId; }
    public UUID                             getTenantId()    { return tenantId; }
    public Money                            getAmount()      { return amount; }
    public PaymentMethod                    getMethod()      { return method; }
    public PaymentTransactionStatus         getStatus()      { return status; }
    public String                           getReference()   { return reference; }
    public LocalDate                        getPaymentDate() { return paymentDate; }
    public Instant                          getCreatedAt()   { return createdAt; }

    /** Reconstitution constructor. */
    public Payment(PaymentId id, UUID invoiceId, UUID tenantId, Money amount,
                   PaymentMethod method, PaymentTransactionStatus status,
                   String reference, LocalDate paymentDate, Instant createdAt) {
        this.id          = id;
        this.invoiceId   = invoiceId;
        this.tenantId    = tenantId;
        this.amount      = amount;
        this.method      = method;
        this.status      = status;
        this.reference   = reference;
        this.paymentDate = paymentDate;
        this.createdAt   = createdAt;
    }

    // ── Factories ──────────────────────────────────────────────────────────

    public static Payment recordCash(UUID invoiceId, UUID tenantId, Money amount, LocalDate paymentDate) {
        var id = PaymentId.generate();
        var p  = new Payment(id, invoiceId, tenantId, amount,
                PaymentMethod.CASH, PaymentTransactionStatus.COMPLETED,
                null, paymentDate, Instant.now());
        p.registerEvent(new PaymentRecordedEvent(UUID.randomUUID(), Instant.now(),
                id, invoiceId, tenantId, amount.amount(), PaymentMethod.CASH));
        return p;
    }

    public static Payment recordMpesa(UUID invoiceId, UUID tenantId, Money amount, LocalDate paymentDate) {
        var id = PaymentId.generate();
        // Status starts as FAILED (placeholder) — updated to COMPLETED on callback
        var p  = new Payment(id, invoiceId, tenantId, amount,
                PaymentMethod.MPESA, PaymentTransactionStatus.FAILED,
                null, paymentDate, Instant.now());
        p.registerEvent(new PaymentRecordedEvent(UUID.randomUUID(), Instant.now(),
                id, invoiceId, tenantId, amount.amount(), PaymentMethod.MPESA));
        return p;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    public void markCompleted(String receiptReference) {
        this.status    = PaymentTransactionStatus.COMPLETED;
        this.reference = receiptReference;
    }

    public void markFailed() {
        this.status = PaymentTransactionStatus.FAILED;
    }
}
