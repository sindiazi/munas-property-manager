package com.example.rentalmanager.tenant.domain.aggregate;

import com.example.rentalmanager.shared.domain.AggregateRoot;
import com.example.rentalmanager.tenant.domain.event.TenantRegisteredEvent;
import com.example.rentalmanager.tenant.domain.event.TenantStatusChangedEvent;
import com.example.rentalmanager.tenant.domain.valueobject.ContactInfo;
import com.example.rentalmanager.tenant.domain.valueobject.PersonalInfo;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: {@code Tenant}
 *
 * <p>Represents a person or legal entity that can enter into lease agreements.
 *
 * <p>Invariants:
 * <ul>
 *   <li>A blacklisted tenant cannot be directly activated.</li>
 *   <li>Email address must be unique system-wide (enforced at the application layer).</li>
 * </ul>
 */
public class Tenant extends AggregateRoot<TenantId> {

    private final TenantId    id;
    private PersonalInfo      personalInfo;
    private ContactInfo       contactInfo;
    private int               creditScore;
    private TenantStatus      status;
    private final Instant     registeredAt;
    /** Plain-text National ID number — encrypted at the persistence layer before storage. */
    private String            nationalIdNo;

    @Override public TenantId    getId()            { return id; }
    public PersonalInfo          getPersonalInfo()   { return personalInfo; }
    public ContactInfo           getContactInfo()    { return contactInfo; }
    public int                   getCreditScore()    { return creditScore; }
    public TenantStatus          getStatus()         { return status; }
    public Instant               getRegisteredAt()   { return registeredAt; }
    public String                getNationalIdNo()   { return nationalIdNo; }

    /** Reconstitution constructor. */
    public Tenant(TenantId id, PersonalInfo personalInfo, ContactInfo contactInfo,
                  int creditScore, TenantStatus status, Instant registeredAt, String nationalIdNo) {
        this.id           = id;
        this.personalInfo = personalInfo;
        this.contactInfo  = contactInfo;
        this.creditScore  = creditScore;
        this.status       = status;
        this.registeredAt = registeredAt;
        this.nationalIdNo = nationalIdNo;
    }

    // ── Factory ────────────────────────────────────────────────────────────

    public static Tenant register(PersonalInfo personalInfo, ContactInfo contactInfo,
                                   int creditScore, String nationalIdNo) {
        var id     = TenantId.generate();
        var tenant = new Tenant(id, personalInfo, contactInfo, creditScore,
                TenantStatus.INACTIVE, Instant.now(), nationalIdNo);
        tenant.registerEvent(new TenantRegisteredEvent(UUID.randomUUID(), Instant.now(),
                id, personalInfo.fullName(), contactInfo.email()));
        return tenant;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    public void activate() {
        if (status == TenantStatus.BLACKLISTED) {
            throw new IllegalStateException("Blacklisted tenants cannot be activated directly");
        }
        changeStatus(TenantStatus.ACTIVE);
    }

    public void deactivate() {
        changeStatus(TenantStatus.INACTIVE);
    }

    public void blacklist() {
        changeStatus(TenantStatus.BLACKLISTED);
    }

    public void updateContactInfo(ContactInfo newContactInfo) {
        this.contactInfo = newContactInfo;
    }

    public void updateCreditScore(int newScore) {
        if (newScore < 300 || newScore > 850) {
            throw new IllegalArgumentException("Credit score must be in range [300, 850]");
        }
        this.creditScore = newScore;
    }

    private void changeStatus(TenantStatus newStatus) {
        var previous = this.status;
        this.status  = newStatus;
        registerEvent(new TenantStatusChangedEvent(UUID.randomUUID(), Instant.now(),
                id, previous, newStatus));
    }
}
