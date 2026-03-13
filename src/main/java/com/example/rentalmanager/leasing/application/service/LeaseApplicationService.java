package com.example.rentalmanager.leasing.application.service;

import com.example.rentalmanager.leasing.application.dto.command.CreateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.command.TerminateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.response.LeaseResponse;
import com.example.rentalmanager.leasing.application.port.input.*;
import com.example.rentalmanager.leasing.application.port.output.LeasePersistencePort;
import com.example.rentalmanager.leasing.domain.aggregate.Lease;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseId;
import com.example.rentalmanager.leasing.domain.valueobject.LeaseTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Application Service for the Leasing bounded context. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseApplicationService
        implements CreateLeaseUseCase, ActivateLeaseUseCase,
                   TerminateLeaseUseCase, GetLeaseUseCase {

    private final LeasePersistencePort     persistencePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mono<LeaseResponse> createLease(CreateLeaseCommand cmd) {
        // Guard: unit must not have an active lease
        return persistencePort.findActiveLeaseByUnitId(cmd.unitId())
                .flatMap(existing -> Mono.<LeaseResponse>error(
                        new IllegalStateException("Unit already has an active lease")))
                .switchIfEmpty(Mono.defer(() -> {
                    var term  = new LeaseTerm(cmd.startDate(), cmd.endDate());
                    var lease = Lease.create(cmd.tenantId(), cmd.propertyId(), cmd.unitId(),
                            term, cmd.monthlyRent(), cmd.securityDeposit());
                    return persistencePort.save(lease)
                            .doOnSuccess(this::publishAndClear)
                            .map(this::toResponse);
                }));
    }

    @Override
    @Transactional
    public Mono<LeaseResponse> activateLease(UUID leaseId) {
        return persistencePort.findById(LeaseId.of(leaseId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Lease not found: " + leaseId)))
                .flatMap(lease -> {
                    lease.activate();
                    return persistencePort.save(lease);
                })
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public Mono<LeaseResponse> terminateLease(TerminateLeaseCommand cmd) {
        return persistencePort.findById(LeaseId.of(cmd.leaseId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Lease not found: " + cmd.leaseId())))
                .flatMap(lease -> {
                    lease.terminate(cmd.reason());
                    return persistencePort.save(lease);
                })
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
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
