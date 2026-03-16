package com.example.rentalmanager.tenant.application.service;

import com.example.rentalmanager.shared.infrastructure.security.SsnEncryptionService;
import com.example.rentalmanager.tenant.application.dto.command.RegisterTenantCommand;
import com.example.rentalmanager.tenant.application.dto.command.UpdateTenantCommand;
import com.example.rentalmanager.tenant.application.dto.response.TenantResponse;
import com.example.rentalmanager.tenant.application.port.input.ActivateTenantUseCase;
import com.example.rentalmanager.tenant.application.port.input.GetTenantUseCase;
import com.example.rentalmanager.tenant.application.port.input.RegisterTenantUseCase;
import com.example.rentalmanager.tenant.application.port.output.TenantPersistencePort;
import com.example.rentalmanager.tenant.domain.aggregate.Tenant;
import com.example.rentalmanager.tenant.domain.valueobject.ContactInfo;
import com.example.rentalmanager.tenant.domain.valueobject.PersonalInfo;
import com.example.rentalmanager.tenant.domain.valueobject.TenantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Application Service for the Tenant bounded context. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantApplicationService implements RegisterTenantUseCase, GetTenantUseCase, ActivateTenantUseCase {

    private final TenantPersistencePort persistencePort;
    private final DomainEventPublisher  eventPublisher;
    private final SsnEncryptionService  ssnEncryptionService;

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
                            cmd.creditScore(), cmd.nationalIdNo());

                    return persistencePort.save(tenant)
                            .doOnSuccess(saved -> {
                                saved.getDomainEvents().forEach(eventPublisher::publish);
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

    @Override
    @Transactional
    public Mono<TenantResponse> activate(UUID tenantId) {
        return persistencePort.findById(TenantId.of(tenantId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Tenant not found: " + tenantId)))
                .flatMap(tenant -> {
                    tenant.activate();
                    return persistencePort.save(tenant)
                            .doOnSuccess(saved -> {
                                saved.getDomainEvents().forEach(eventPublisher::publish);
                                saved.clearDomainEvents();
                            });
                })
                .map(this::toResponse);
    }

    private TenantResponse toResponse(Tenant t) {
        String maskedId = ssnEncryptionService.mask(t.getNationalIdNo());
        return new TenantResponse(
                t.getId().value(),
                t.getPersonalInfo().firstName(), t.getPersonalInfo().lastName(),
                t.getContactInfo().email(), t.getContactInfo().phoneNumber(),
                maskedId,
                t.getCreditScore(), t.getStatus(), t.getRegisteredAt());
    }

    /**
     * Resolves a plain 9-digit National ID number to the matching tenant UUID.
     * Used by the lease creation flow when {@code tenantId} is submitted as a National ID.
     */
    public Mono<UUID> resolveTenantIdByNationalIdNo(String plainNationalId) {
        String hash = ssnEncryptionService.computeLookupHash(plainNationalId);
        return persistencePort.findByNationalIdNoHash(hash)
                .map(t -> t.getId().value())
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("No tenant found for provided National ID number")));
    }
}
