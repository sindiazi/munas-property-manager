package com.example.rentalmanager.property.application.service;

import com.example.rentalmanager.property.application.dto.command.AddPropertyUnitCommand;
import com.example.rentalmanager.property.application.dto.command.CreatePropertyCommand;
import com.example.rentalmanager.property.application.dto.command.UpdatePropertyCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyResponse;
import com.example.rentalmanager.property.application.dto.response.PropertyUnitResponse;
import com.example.rentalmanager.property.application.port.input.AddPropertyUnitUseCase;
import com.example.rentalmanager.property.application.port.input.CreatePropertyUseCase;
import com.example.rentalmanager.property.application.port.input.GetPropertyUseCase;
import com.example.rentalmanager.property.application.port.input.UpdatePropertyUseCase;
import com.example.rentalmanager.property.application.port.output.PropertyPersistencePort;
import com.example.rentalmanager.property.domain.aggregate.Property;
import com.example.rentalmanager.property.domain.aggregate.PropertyUnit;
import com.example.rentalmanager.property.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Application Service for the Property bounded context.
 *
 * <p>Orchestrates domain objects, persists state via the output port, and
 * publishes domain events after successful persistence. This service is the
 * sole implementation of all property-related input use-case ports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyApplicationService
        implements CreatePropertyUseCase, AddPropertyUnitUseCase,
                   UpdatePropertyUseCase, GetPropertyUseCase {

    private final PropertyPersistencePort persistencePort;
    private final ApplicationEventPublisher eventPublisher;

    // ── CreatePropertyUseCase ──────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<PropertyResponse> createProperty(CreatePropertyCommand cmd) {
        log.debug("Creating property '{}' for owner {}", cmd.name(), cmd.ownerId());

        var address = new Address(cmd.street(), cmd.city(), cmd.state(), cmd.zipCode(), cmd.country());
        var property = Property.create(OwnerId.of(cmd.ownerId()), cmd.name(), address, cmd.type());

        return persistencePort.save(property)
                .doOnSuccess(saved -> publishEvents(saved))
                .map(this::toResponse);
    }

    // ── AddPropertyUnitUseCase ─────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<PropertyResponse> addUnit(AddPropertyUnitCommand cmd) {
        log.debug("Adding unit '{}' to property {}", cmd.unitNumber(), cmd.propertyId());

        var propertyId = PropertyId.of(cmd.propertyId());

        return persistencePort.findById(propertyId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Property not found: " + cmd.propertyId())))
                .flatMap(property -> {
                    var rent = MonthlyRent.of(cmd.monthlyRentAmount(), cmd.currencyCode());
                    var unit = new PropertyUnit(
                            UnitId.generate(), cmd.unitNumber(),
                            cmd.bedrooms(), cmd.bathrooms(),
                            cmd.squareFootage(), rent);
                    property.addUnit(unit);
                    return persistencePort.save(property);
                })
                .doOnSuccess(this::publishEvents)
                .map(this::toResponse);
    }

    // ── UpdatePropertyUseCase ──────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<PropertyResponse> updateProperty(UpdatePropertyCommand cmd) {
        var propertyId = PropertyId.of(cmd.propertyId());

        return persistencePort.findById(propertyId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Property not found: " + cmd.propertyId())))
                .flatMap(property -> {
                    Address newAddress = null;
                    if (cmd.street() != null) {
                        newAddress = new Address(cmd.street(), cmd.city(), cmd.state(),
                                cmd.zipCode(), cmd.country());
                    }
                    property.updateDetails(cmd.name(), newAddress);
                    return persistencePort.save(property);
                })
                .map(this::toResponse);
    }

    // ── GetPropertyUseCase ─────────────────────────────────────────────────

    @Override
    public Mono<PropertyResponse> getById(UUID propertyId) {
        return persistencePort.findById(PropertyId.of(propertyId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Property not found: " + propertyId)))
                .map(this::toResponse);
    }

    @Override
    public Flux<PropertyResponse> getByOwnerId(UUID ownerId) {
        return persistencePort.findByOwnerId(OwnerId.of(ownerId)).map(this::toResponse);
    }

    @Override
    public Flux<PropertyResponse> getAll() {
        return persistencePort.findAll().map(this::toResponse);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void publishEvents(Property property) {
        property.getDomainEvents().forEach(eventPublisher::publishEvent);
        property.clearDomainEvents();
    }

    private PropertyResponse toResponse(Property p) {
        var units = p.getUnits().stream().map(this::toUnitResponse).toList();
        return new PropertyResponse(
                p.getId().value(), p.getOwnerId().value(),
                p.getName(),
                p.getAddress().street(), p.getAddress().city(),
                p.getAddress().state(), p.getAddress().zipCode(),
                p.getAddress().country(),
                p.getType(), units, p.getCreatedAt());
    }

    private PropertyUnitResponse toUnitResponse(PropertyUnit u) {
        return new PropertyUnitResponse(
                u.getId().value(), u.getUnitNumber(),
                u.getBedrooms(), u.getBathrooms(), u.getSquareFootage(),
                u.getMonthlyRent().amount(), u.getMonthlyRent().currency().getCurrencyCode(),
                u.getStatus());
    }
}
