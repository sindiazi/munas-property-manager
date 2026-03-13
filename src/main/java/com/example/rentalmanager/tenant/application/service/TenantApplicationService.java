package com.example.rentalmanager.tenant.application.service;

import com.example.rentalmanager.tenant.application.dto.command.RegisterTenantCommand;
import com.example.rentalmanager.tenant.application.dto.command.UpdateTenantCommand;
import com.example.rentalmanager.tenant.application.dto.response.TenantResponse;
import com.example.rentalmanager.tenant.application.port.input.GetTenantUseCase;
import com.example.rentalmanager.tenant.application.port.input.RegisterTenantUseCase;
import com.example.rentalmanager.tenant.application.port.output.TenantPersistencePort;
import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.ContactInfo;
import com.example.rentalmanager.tenant.domain.valueobject.PersonalInfo;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Application Service for the Tenant bounded context. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantApplicationService implements RegisterTenantUseCase, GetTenantUseCase {

    private final TenantPersistencePort    persistencePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mono<TenantResponse> register(RegisterTenantCommand cmd) {
        return persistencePort.existsByEmail(cmd.email())
                .flatMap(exists -> {
                    if (exists) return Mono.error(
                            new IllegalArgumentException("Email already registered: " + cmd.email()));

                    var tenant = Tenant.register(
                            new PersonalInfo(cmd.firstName(), cmd.lastName(), cmd.nationalId()),
                            new ContactInfo(cmd.email(), cmd.phoneNumber()),
                            cmd.creditScore());

                    return persistencePort.save(tenant)
                            .doOnSuccess(saved -> {
                                saved.getDomainEvents().forEach(eventPublisher::publishEvent);
                                saved.clearDomainEvents();
                            })
                            .map(this::toResponse);
                });
    }

    @Transactional
    public Mono<TenantResponse> updateContactInfo(UpdateTenantCommand cmd) {
        return persistencePort.findById(TenantId.of(cmd.tenantId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Tenant not found")))
                .flatMap(tenant -> {
                    tenant.updateContactInfo(new ContactInfo(cmd.email(), cmd.phoneNumber()));
                    return persistencePort.save(tenant);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<TenantResponse> getById(UUID tenantId) {
        return persistencePort.findById(TenantId.of(tenantId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Tenant not found: " + tenantId)))
                .map(this::toResponse);
    }

    @Override
    public Flux<TenantResponse> getAll() {
        return persistencePort.findAll().map(this::toResponse);
    }

    private TenantResponse toResponse(Tenant t) {
        return new TenantResponse(
                t.getId().value(),
                t.getPersonalInfo().firstName(), t.getPersonalInfo().lastName(),
                t.getContactInfo().email(), t.getContactInfo().phoneNumber(),
                t.getCreditScore(), t.getStatus(), t.getRegisteredAt());
    }
}
