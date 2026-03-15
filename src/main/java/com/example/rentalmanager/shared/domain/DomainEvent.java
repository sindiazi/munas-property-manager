package com.example.rentalmanager.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Sealed marker interface for all Domain Events.
 *
 * <p>Every concrete event must be a {@code record} that implements this
 * interface and is listed as a permitted subtype. This ensures exhaustive
 * pattern matching at the switch level.
 *
 * <p>Events are named in the past tense (e.g. {@code PropertyCreated},
 * {@code LeaseActivated}) and are immutable by design.
 */
public interface DomainEvent {

    /** Globally unique event identifier. */
    UUID eventId();

    /** Wall-clock time when the event was raised. */
    Instant occurredOn();
}
