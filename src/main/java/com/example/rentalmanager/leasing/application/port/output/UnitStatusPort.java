package com.example.rentalmanager.leasing.application.port.output;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port used by the Leasing context to keep unit status in sync
 * with lease lifecycle events.
 *
 * <p>Implemented by the Property infrastructure layer to avoid direct
 * cross-context coupling.
 */
public interface UnitStatusPort {
    Mono<Void> markOccupied(UUID unitId);
    Mono<Void> markAvailable(UUID unitId);
}
