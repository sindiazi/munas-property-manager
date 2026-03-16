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
 * <p>Updates are fire-and-forget ({@code .subscribe()}) because unit status
 * is eventually consistent by design. If the update fails, the error is logged
 * and can be retried via event replay.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnitStatusSyncHandler {

    private final PropertyUnitR2dbcRepository unitRepo;

    @EventListener
    public void on(LeaseActivatedEvent event) {
        log.debug("[UNIT-SYNC] LeaseActivated → marking unit {} OCCUPIED", event.unitId());
        updateStatus(event.unitId(), UnitStatus.OCCUPIED);
    }

    @EventListener
    public void on(LeaseTerminatedEvent event) {
        log.debug("[UNIT-SYNC] LeaseTerminated → marking unit {} AVAILABLE", event.unitId());
        updateStatus(event.unitId(), UnitStatus.AVAILABLE);
    }

    @EventListener
    public void on(LeaseExpiredEvent event) {
        log.debug("[UNIT-SYNC] LeaseExpired → marking unit {} AVAILABLE", event.unitId());
        updateStatus(event.unitId(), UnitStatus.AVAILABLE);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void updateStatus(java.util.UUID unitId, UnitStatus status) {
        unitRepo.findById(unitId)
                .flatMap(entity -> {
                    entity.setStatus(status);
                    return unitRepo.save(entity);
                })
                .doOnError(e -> log.error("[UNIT-SYNC] Failed to update status for unit {}: {}",
                        unitId, e.getMessage()))
                .subscribe();
    }
}
