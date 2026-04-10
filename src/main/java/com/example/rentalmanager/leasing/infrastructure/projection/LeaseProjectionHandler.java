package com.example.rentalmanager.leasing.infrastructure.projection;

import com.example.rentalmanager.leasing.domain.event.LeaseActivatedEvent;
import com.example.rentalmanager.leasing.domain.event.LeaseExpiredEvent;
import com.example.rentalmanager.leasing.domain.event.LeaseTerminatedEvent;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.TenantOccupiedUnitEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryKey;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.TenantOccupiedUnitRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.UnitRentalHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Subscribes to lease domain events and updates the CQRS read-model projection tables:
 * <ul>
 *   <li>{@code tenant_occupied_unit} — current occupancy per tenant</li>
 *   <li>{@code unit_rental_history} — full rental timeline per unit</li>
 * </ul>
 *
 * <p>Projection updates are eventually consistent by design. A projection rebuild can be
 * triggered by replaying events or re-running the data seeder. Listener methods return
 * {@code Mono<Void>} so Spring subscribes on their behalf — no explicit {@code .subscribe()} needed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaseProjectionHandler {

    private final TenantOccupiedUnitRepository occupancyRepo;
    private final UnitRentalHistoryRepository  historyRepo;

    // ── Lease Activated ────────────────────────────────────────────────────

    @EventListener
    public Mono<Void> on(LeaseActivatedEvent event) {
        log.debug("[PROJECTION] LeaseActivated → updating occupancy + history for unit {}",
                event.unitId());

        var occupancy = TenantOccupiedUnitEntity.builder()
                .tenantId(event.tenantId())
                .unitId(event.unitId())
                .propertyId(event.propertyId())
                .leaseId(event.leaseId().value())
                .monthlyRent(event.monthlyRent())
                .leaseStart(event.startDate())
                .leaseEnd(event.endDate())
                .occupiedSince(event.occurredOn())
                .build();

        var historyKey = new UnitRentalHistoryKey(
                event.unitId(), event.startDate(), event.leaseId().value());
        var history = UnitRentalHistoryEntity.builder()
                .key(historyKey)
                .tenantId(event.tenantId())
                .propertyId(event.propertyId())
                .monthlyRent(event.monthlyRent())
                .leaseEnd(event.endDate())
                .status("ACTIVE")
                .build();

        return Mono.when(occupancyRepo.save(occupancy), historyRepo.save(history))
                .onErrorResume(e -> {
                    log.error("[PROJECTION] Failed to update on LeaseActivated: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }

    // ── Lease Terminated ───────────────────────────────────────────────────

    @EventListener
    public Mono<Void> on(LeaseTerminatedEvent event) {
        log.debug("[PROJECTION] LeaseTerminated → clearing occupancy, updating history for unit {}",
                event.unitId());

        var historyKey = new UnitRentalHistoryKey(
                event.unitId(), event.startDate(), event.leaseId().value());
        var history = UnitRentalHistoryEntity.builder()
                .key(historyKey)
                .tenantId(event.tenantId())
                .propertyId(event.propertyId())
                .monthlyRent(event.monthlyRent())
                .leaseEnd(event.endDate())
                .status("TERMINATED")
                .build();

        return Mono.when(occupancyRepo.deleteById(event.tenantId()), historyRepo.save(history))
                .onErrorResume(e -> {
                    log.error("[PROJECTION] Failed to update on LeaseTerminated: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }

    // ── Lease Expired ──────────────────────────────────────────────────────

    @EventListener
    public Mono<Void> on(LeaseExpiredEvent event) {
        log.debug("[PROJECTION] LeaseExpired → clearing occupancy, updating history for unit {}",
                event.unitId());

        var historyKey = new UnitRentalHistoryKey(
                event.unitId(), event.startDate(), event.leaseId().value());
        var history = UnitRentalHistoryEntity.builder()
                .key(historyKey)
                .tenantId(event.tenantId())
                .propertyId(event.propertyId())
                .monthlyRent(event.monthlyRent())
                .leaseEnd(event.endDate())
                .status("EXPIRED")
                .build();

        return Mono.when(occupancyRepo.deleteById(event.tenantId()), historyRepo.save(history))
                .onErrorResume(e -> {
                    log.error("[PROJECTION] Failed to update on LeaseExpired: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }
}
