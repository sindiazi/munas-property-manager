package com.example.rentalmanager.leasing.domain.aggregate;

import com.example.rentalmanager.leasing.domain.event.LeaseActivatedEvent;
import com.example.rentalmanager.leasing.domain.event.LeaseCreatedEvent;
import com.example.rentalmanager.leasing.domain.event.LeaseTerminatedEvent;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseTerm;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
import com.example.rentalmanager.shared.domain.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: {@code Lease}
 *
 * <p>Represents the rental agreement between a tenant and a property owner for
 * a specific unit. Manages the full lifecycle: DRAFT → ACTIVE → EXPIRED/TERMINATED.
 *
 * <p>Cross-context references ({@code tenantId}, {@code propertyId}, {@code unitId})
 * are held as plain UUIDs — never as object references — to maintain bounded context
 * isolation.
 */
@Getter
public class Lease extends AggregateRoot<LeaseId> {

    private final LeaseId    id;
    private final UUID       tenantId;
    private final UUID       propertyId;
    private final UUID       unitId;
    private final LeaseTerm  term;
    private final BigDecimal monthlyRent;
    private final BigDecimal securityDeposit;
    private LeaseStatus      status;
    private String           terminationReason;
    private final Instant    createdAt;

    /** Reconstitution constructor. */
    public Lease(LeaseId id, UUID tenantId, UUID propertyId, UUID unitId,
                 LeaseTerm term, BigDecimal monthlyRent, BigDecimal securityDeposit,
                 LeaseStatus status, String terminationReason, Instant createdAt) {
        this.id                = id;
        this.tenantId          = tenantId;
        this.propertyId        = propertyId;
        this.unitId            = unitId;
        this.term              = term;
        this.monthlyRent       = monthlyRent;
        this.securityDeposit   = securityDeposit;
        this.status            = status;
        this.terminationReason = terminationReason;
        this.createdAt         = createdAt;
    }

    // ── Factory ────────────────────────────────────────────────────────────

    public static Lease create(UUID tenantId, UUID propertyId, UUID unitId,
                               LeaseTerm term, BigDecimal monthlyRent, BigDecimal securityDeposit) {
        if (monthlyRent.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Monthly rent must be positive");
        if (securityDeposit.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Security deposit cannot be negative");

        var id    = LeaseId.generate();
        var lease = new Lease(id, tenantId, propertyId, unitId, term, monthlyRent,
                securityDeposit, LeaseStatus.DRAFT, null, Instant.now());
        lease.registerEvent(new LeaseCreatedEvent(UUID.randomUUID(), Instant.now(),
                id, tenantId, propertyId, unitId));
        return lease;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    /** Transitions the lease from DRAFT to ACTIVE. */
    public void activate() {
        if (status != LeaseStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT leases can be activated, current status: " + status);
        }
        this.status = LeaseStatus.ACTIVE;
        registerEvent(new LeaseActivatedEvent(UUID.randomUUID(), Instant.now(), id, tenantId, unitId));
    }

    /** Terminates an active lease early. */
    public void terminate(String reason) {
        if (status != LeaseStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE leases can be terminated, current status: " + status);
        }
        this.status            = LeaseStatus.TERMINATED;
        this.terminationReason = reason;
        registerEvent(new LeaseTerminatedEvent(UUID.randomUUID(), Instant.now(), id, tenantId, unitId, reason));
    }

    /** Expires the lease after its natural end date is reached. */
    public void expire() {
        if (status != LeaseStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE leases can expire");
        }
        this.status = LeaseStatus.EXPIRED;
    }
}
