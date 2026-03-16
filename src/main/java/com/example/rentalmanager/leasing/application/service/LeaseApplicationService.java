package com.example.rentalmanager.leasing.application.service;

import com.example.rentalmanager.leasing.application.dto.command.CreateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.command.TerminateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.response.LeaseResponse;
import com.example.rentalmanager.leasing.application.port.input.*;
import com.example.rentalmanager.leasing.application.port.output.LeasePersistencePort;
import com.example.rentalmanager.leasing.application.port.output.UnitStatusPort;
import com.example.rentalmanager.leasing.domain.aggregate.Lease;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseTerm;
import com.example.rentalmanager.tenant.application.service.TenantApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

/** Application Service for the Leasing bounded context. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseApplicationService
        implements CreateLeaseUseCase, ActivateLeaseUseCase,
                   TerminateLeaseUseCase, GetLeaseUseCase {

    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile("^\\d{9}$");

    private final LeasePersistencePort       persistencePort;
    private final UnitStatusPort             unitStatusPort;
    private final TenantApplicationService   tenantApplicationService;
    private final ApplicationEventPublisher  eventPublisher;

    @Override
    @Transactional
    public Mono<LeaseResponse> createLease(CreateLeaseCommand cmd) {
        return resolveTenantId(cmd.tenantId())
                .flatMap(tenantUuid -> persistencePort.findNonTerminalLeaseByUnitId(cmd.unitId())
                        .flatMap(existing -> Mono.<LeaseResponse>error(new IllegalStateException(
                                "Unit already has a " + existing.getStatus().name().toLowerCase()
                                + " lease — a unit can only hold one active lease at a time")))
                        .switchIfEmpty(Mono.defer(() -> {
                            var term  = new LeaseTerm(cmd.startDate(), cmd.endDate());
                            var lease = Lease.create(tenantUuid, cmd.propertyId(), cmd.unitId(),
                                    term, cmd.monthlyRent(), cmd.securityDeposit());
                            return persistencePort.save(lease)
                                    .doOnSuccess(this::publishAndClear)
                                    .map(this::toResponse);
                        })));
    }

    @Override
    @Transactional
    public Mono<LeaseResponse> activateLease(UUID leaseId) {
        return persistencePort.findById(LeaseId.of(leaseId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Lease not found: " + leaseId)))
                .flatMap(lease -> {
                    lease.activate();
                    return persistencePort.save(lease)
                            .doOnSuccess(this::publishAndClear)
                            // Keep unit in sync: OCCUPIED
                            .flatMap(saved -> unitStatusPort.markOccupied(saved.getUnitId())
                                    .thenReturn(saved));
                })
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public Mono<LeaseResponse> terminateLease(TerminateLeaseCommand cmd) {
        return persistencePort.findById(LeaseId.of(cmd.leaseId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Lease not found: " + cmd.leaseId())))
                .flatMap(lease -> {
                    lease.terminate(cmd.reason());
                    return persistencePort.save(lease)
                            .doOnSuccess(this::publishAndClear)
                            // Keep unit in sync: AVAILABLE
                            .flatMap(saved -> unitStatusPort.markAvailable(saved.getUnitId())
                                    .thenReturn(saved));
                })
                .map(this::toResponse);
    }

    @Override
    public Flux<LeaseResponse> getAll() {
        return persistencePort.findAll().map(this::toResponse);
    }

    @Override
    public Mono<LeaseResponse> getById(UUID leaseId) {
        return persistencePort.findById(LeaseId.of(leaseId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Lease not found: " + leaseId)))
                .map(this::toResponse);
    }

    @Override
    public Flux<LeaseResponse> getByTenantId(UUID tenantId) {
        return persistencePort.findByTenantId(tenantId).map(this::toResponse);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Accepts either a UUID string or a 9-digit SSN.
     * If SSN is detected, resolves it to the corresponding tenant UUID.
     */
    private Mono<UUID> resolveTenantId(String tenantIdentifier) {
        if (NATIONAL_ID_PATTERN.matcher(tenantIdentifier).matches()) {
            log.debug("tenantId looks like a National ID number — resolving via tenant lookup");
            return tenantApplicationService.resolveTenantIdByNationalIdNo(tenantIdentifier);
        }
        try {
            return Mono.just(UUID.fromString(tenantIdentifier));
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException(
                    "tenantId must be a valid UUID or a 9-digit National ID number, got: " + tenantIdentifier));
        }
    }

    private void publishAndClear(Lease lease) {
        lease.getDomainEvents().forEach(eventPublisher::publishEvent);
        lease.clearDomainEvents();
    }

    private LeaseResponse toResponse(Lease l) {
        return new LeaseResponse(
                l.getId().value(), l.getTenantId(), l.getPropertyId(), l.getUnitId(),
                l.getTerm().startDate(), l.getTerm().endDate(),
                l.getMonthlyRent(), l.getSecurityDeposit(),
                l.getStatus(), l.getTerminationReason(), l.getCreatedAt());
    }
}
