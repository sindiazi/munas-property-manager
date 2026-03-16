package com.example.rentalmanager.maintenance.application.service;

import com.example.rentalmanager.maintenance.application.dto.command.CreateMaintenanceRequestCommand;
import com.example.rentalmanager.maintenance.application.dto.command.UpdateMaintenanceStatusCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceRequestResponse;
import com.example.rentalmanager.maintenance.application.port.input.*;
import com.example.rentalmanager.maintenance.application.port.output.MaintenancePersistencePort;
import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceRequest;
import com.example.rentalmanager.maintenance.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Application Service for the Maintenance bounded context. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceApplicationService
        implements CreateMaintenanceRequestUseCase, UpdateMaintenanceRequestUseCase,
                   GetMaintenanceRequestUseCase {

    private final MaintenancePersistencePort persistencePort;
    private final DomainEventPublisher       eventPublisher;

    @Override
    @Transactional
    public Mono<MaintenanceRequestResponse> create(CreateMaintenanceRequestCommand cmd) {
        var request = MaintenanceRequest.open(
                cmd.propertyId(), cmd.unitId(), cmd.tenantId(),
                new WorkDescription(cmd.problemDescription(), null),
                cmd.priority());
        return persistencePort.save(request)
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public Mono<MaintenanceRequestResponse> updateStatus(UpdateMaintenanceStatusCommand cmd) {
        return persistencePort.findById(RequestId.of(cmd.requestId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Request not found: " + cmd.requestId())))
                .flatMap(request -> {
                    switch (cmd.newStatus()) {
                        case ASSIGNED     -> request.assign();
                        case IN_PROGRESS  -> request.startWork();
                        case COMPLETED    -> request.complete(cmd.resolutionNotes());
                        case CANCELLED    -> request.cancel();
                        default           -> throw new IllegalArgumentException(
                                "Unsupported status transition: " + cmd.newStatus());
                    }
                    return persistencePort.save(request);
                })
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    public Mono<MaintenanceRequestResponse> getById(UUID requestId) {
        return persistencePort.findById(RequestId.of(requestId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Request not found: " + requestId)))
                .map(this::toResponse);
    }

    @Override
    public Flux<MaintenanceRequestResponse> getByPropertyId(UUID propertyId) {
        return persistencePort.findByPropertyId(propertyId).map(this::toResponse);
    }

    @Override
    public Flux<MaintenanceRequestResponse> getByTenantId(UUID tenantId) {
        return persistencePort.findByTenantId(tenantId).map(this::toResponse);
    }

    private void publishAndClear(MaintenanceRequest request) {
        request.getDomainEvents().forEach(eventPublisher::publish);
        request.clearDomainEvents();
    }

    private MaintenanceRequestResponse toResponse(MaintenanceRequest r) {
        return new MaintenanceRequestResponse(
                r.getId().value(), r.getPropertyId(), r.getUnitId(), r.getTenantId(),
                r.getDescription().problemDescription(), r.getDescription().resolutionNotes(),
                r.getPriority(), r.getStatus(), r.getRequestedAt(), r.getCompletedAt());
    }
}
