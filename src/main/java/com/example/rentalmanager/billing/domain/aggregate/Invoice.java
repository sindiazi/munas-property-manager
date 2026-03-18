package com.example.rentalmanager.billing.domain.aggregate;

import com.example.rentalmanager.billing.domain.event.*;
import com.example.rentalmanager.billing.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: {@code Invoice}
 *
 * <p>Represents a billing obligation — a tenant owes a specific amount
 * by a specific date. Supports partial payments.
 */
public class Invoice extends AggregateRoot<InvoiceId> {

    private final InvoiceId   id;
    private final UUID        leaseId;
    private final UUID        tenantId;
    private final Money       amountDue;
    private Money             amountPaid;
    private final LocalDate   dueDate;
    private LocalDate         paidDate;
    private InvoiceStatus     status;
    private final InvoiceType type;
    private final Instant     createdAt;

    @Override public InvoiceId  getId()        { return id; }
    public UUID                 getLeaseId()   { return leaseId; }
    public UUID                 getTenantId()  { return tenantId; }
    public Money                getAmountDue() { return amountDue; }
    public Money                getAmountPaid(){ return amountPaid; }
    public LocalDate            getDueDate()   { return dueDate; }
    public LocalDate            getPaidDate()  { return paidDate; }
    public InvoiceStatus        getStatus()    { return status; }
    public InvoiceType          getType()      { return type; }
    public Instant              getCreatedAt() { return createdAt; }

    /** Reconstitution constructor. */
    public Invoice(InvoiceId id, UUID leaseId, UUID tenantId, Money amountDue, Money amountPaid,
                   LocalDate dueDate, LocalDate paidDate, InvoiceStatus status,
                   InvoiceType type, Instant createdAt) {
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

    public static Invoice create(UUID leaseId, UUID tenantId, Money amountDue,
                                 LocalDate dueDate, InvoiceType type) {
        var id       = InvoiceId.generate();
        var zeroPaid = Money.of(BigDecimal.ZERO, amountDue.currency().getCurrencyCode());
        var invoice  = new Invoice(id, leaseId, tenantId, amountDue, zeroPaid,
                dueDate, null, InvoiceStatus.PENDING, type, Instant.now());
        invoice.registerEvent(new InvoiceCreatedEvent(UUID.randomUUID(), Instant.now(),
                id, leaseId, tenantId, amountDue.amount(), type));
        return invoice;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    /** Records a (possibly partial) payment. */
    public void receive(Money received, LocalDate paymentDate) {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already " + status);
        }
        this.amountPaid = amountPaid.add(received);
        this.paidDate   = paymentDate;

        if (amountPaid.amount().compareTo(amountDue.amount()) >= 0) {
            this.status = InvoiceStatus.PAID;
        } else {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
        registerEvent(new InvoiceSettledEvent(UUID.randomUUID(), Instant.now(),
                id, tenantId, received.amount()));
    }

    /** Marks the invoice as overdue. */
    public void markOverdue() {
        if (status != InvoiceStatus.PENDING && status != InvoiceStatus.PARTIALLY_PAID) {
            throw new IllegalStateException("Cannot mark as overdue, current status: " + status);
        }
        this.status = InvoiceStatus.OVERDUE;
        registerEvent(new InvoiceOverdueEvent(UUID.randomUUID(), Instant.now(), id, tenantId, dueDate));
    }

    public void cancel() {
        this.status = InvoiceStatus.CANCELLED;
        registerEvent(new InvoiceCancelledEvent(UUID.randomUUID(), Instant.now(), id, tenantId));
    }

    /** Records an M-Pesa-confirmed payment and emits a gateway-specific event. */
    public void receiveViaMpesa(Money received, LocalDate paymentDate,
                                String mpesaReceiptNumber, String phoneNumber) {
        receive(received, paymentDate);
        registerEvent(new MpesaPaymentConfirmedEvent(UUID.randomUUID(), Instant.now(),
                id.value(), tenantId, mpesaReceiptNumber, received.amount(), phoneNumber));
    }

    /** Records an M-Pesa failure or cancellation and emits a gateway-specific event. */
    public void failedViaMpesa(int resultCode, String resultDesc) {
        registerEvent(new MpesaPaymentFailedEvent(UUID.randomUUID(), Instant.now(),
                id.value(), tenantId, resultCode, resultDesc));
    }

    /** Outstanding balance remaining. */
    public BigDecimal outstandingBalance() {
        return amountDue.amount().subtract(amountPaid.amount());
    }
}
