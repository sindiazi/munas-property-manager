package com.example.rentalmanager.property.infrastructure.event;

import com.example.rentalmanager.leasing.domain.event.LeaseActivatedEvent;
import com.example.rentalmanager.leasing.domain.event.LeaseExpiredEvent;
import com.example.rentalmanager.leasing.domain.event.LeaseTerminatedEvent;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyUnitR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Keeps {@code PropertyUnit} status in sync with the Lease lifecycle.
 *
 * <p>The Property bounded context owns unit availability state. Rather than
 * allowing the Leasing context to call into Property directly, this handler
 * subscribes to lease domain events and updates unit status asynchronously
 * (eventually consistent).
 *
 * <p>EDA principle applied here:
 * <ol>
 *   <li>Leasing domain mutates its own aggregate and emits a domain event.</li>
 *   <li>This handler (owned by the Property domain) reacts to that event and
 *       updates Property's own state — unit availability.</li>
 *   <li>No cross-domain method calls; coupling is event-based only.</li>
 * </ol>
 *
 * <p>Unit status is eventually consistent by design — if an update fails the error
 * is logged and can be retried via event replay. Listener methods return
 * {@code Mono<Void>} so Spring subscribes on their behalf.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnitStatusSyncHandler {

    private final PropertyUnitR2dbcRepository unitRepo;

    @EventListener
    public Mono<Void> on(LeaseActivatedEvent event) {
        log.debug("[UNIT-SYNC] LeaseActivated → marking unit {} OCCUPIED", event.unitId());
        return updateStatus(event.unitId(), UnitStatus.OCCUPIED);
    }

    @EventListener
    public Mono<Void> on(LeaseTerminatedEvent event) {
        log.debug("[UNIT-SYNC] LeaseTerminated → marking unit {} AVAILABLE", event.unitId());
        return updateStatus(event.unitId(), UnitStatus.AVAILABLE);
    }

    @EventListener
    public Mono<Void> on(LeaseExpiredEvent event) {
        log.debug("[UNIT-SYNC] LeaseExpired → marking unit {} AVAILABLE", event.unitId());
        return updateStatus(event.unitId(), UnitStatus.AVAILABLE);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Mono<Void> updateStatus(java.util.UUID unitId, UnitStatus status) {
        return unitRepo.findById(unitId)
                .flatMap(entity -> {
                    entity.setStatus(status);
                    return unitRepo.save(entity);
                })
                .onErrorResume(e -> {
                    log.error("[UNIT-SYNC] Failed to update status for unit {}: {}", unitId, e.getMessage(), e);
                    return Mono.empty();
                })
                .then();
    }
}
