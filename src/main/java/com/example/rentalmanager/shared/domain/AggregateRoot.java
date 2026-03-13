package com.example.rentalmanager.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all Aggregate Roots in the system.
 *
 * <p>Maintains a list of uncommitted {@link DomainEvent}s that are collected
 * during state transitions and published by the application layer after the
 * aggregate has been persisted (transactional outbox pattern ready).
 *
 * @param <ID> the type of the aggregate's identity
 */
public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /** Returns the unique identity of this aggregate. */
    public abstract ID getId();

    /**
     * Registers a new domain event to be published after persistence.
     *
     * @param event the event to record
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Returns an unmodifiable snapshot of all pending domain events.
     *
     * @return immutable view of domain events
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears all pending domain events. Called by the application layer after
     * events have been published.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
