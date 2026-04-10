package com.example.rentalmanager.leasing.application.service;

import com.example.rentalmanager.leasing.application.port.output.LeasePersistencePort;
import com.example.rentalmanager.leasing.domain.aggregate.Lease;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Domain policy that runs daily to detect and expire leases whose natural end
 * date has passed.
 *
 * <p>On each execution the policy:
 * <ol>
 *   <li>Loads all {@code ACTIVE} leases from the persistence port.</li>
 *   <li>Filters to those whose {@code endDate} is strictly before today.</li>
 *   <li>Calls {@link Lease#expire()} on each, which transitions the status to
 *       {@code EXPIRED} and registers a {@code LeaseExpiredEvent}.</li>
 *   <li>Persists the updated lease and publishes its domain events.</li>
 * </ol>
 *
 * <p>The {@code LeaseExpiredEvent} is then picked up by:
 * <ul>
 *   <li>{@code UnitStatusSyncHandler} — marks the unit as {@code AVAILABLE}.</li>
 *   <li>{@code LeaseProjectionHandler} — updates the CQRS read projections.</li>
 * </ul>
 *
 * <p>The job runs at 01:00 every day (server local time). Individual lease
 * failures are caught inside {@code flatMap} and logged, so one failure does
 * not abort processing of remaining leases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseExpiryPolicy {

    private final LeasePersistencePort persistencePort;
    private final DomainEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 1 * * *")
    public void expireOverdueLeases() {
        LocalDate today = LocalDate.now();
        log.info("[EXPIRY-POLICY] Checking for expired leases (today = {})", today);

        persistencePort.findByStatus(LeaseStatus.ACTIVE)
                .filter(lease -> lease.getTerm().endDate().isBefore(today))
                .flatMap(lease -> {
                    log.info("[EXPIRY-POLICY] Expiring lease {} (unit {}, ended {})",
                            lease.getId().value(), lease.getUnitId(), lease.getTerm().endDate());
                    lease.expire();
                    return persistencePort.save(lease)
                            .doOnSuccess(this::publishAndClear)
                            .doOnSuccess(saved -> log.debug("[EXPIRY-POLICY] Expired lease {}", saved.getId().value()))
                            .onErrorResume(e -> {
                                log.error("[EXPIRY-POLICY] Failed to expire lease {}: {}",
                                        lease.getId().value(), e.getMessage(), e);
                                return Mono.empty();
                            });
                })
                .doOnComplete(() -> log.info("[EXPIRY-POLICY] Expiry run complete"))
                .blockLast();
    }

    private void publishAndClear(Lease lease) {
        lease.getDomainEvents().forEach(eventPublisher::publish);
        lease.clearDomainEvents();
    }
}
