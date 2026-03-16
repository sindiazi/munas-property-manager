package com.example.rentalmanager.property.application.service;

import com.example.rentalmanager.property.application.dto.command.AddPropertyUnitCommand;
import com.example.rentalmanager.property.application.dto.command.CreatePropertyCommand;
import com.example.rentalmanager.property.application.dto.command.MarkUnitUnavailableCommand;
import com.example.rentalmanager.property.application.dto.command.UpdatePropertyCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyResponse;
import com.example.rentalmanager.property.application.dto.response.PropertyUnitResponse;
import com.example.rentalmanager.property.application.dto.response.UnitUnavailabilityResponse;
import com.example.rentalmanager.property.application.port.input.*;
import com.example.rentalmanager.property.application.port.output.PropertyPersistencePort;
import com.example.rentalmanager.property.application.port.output.UnitPersistencePort;
import com.example.rentalmanager.property.application.port.output.UnitUnavailabilityPersistencePort;
import com.example.rentalmanager.property.domain.aggregate.Property;
import com.example.rentalmanager.property.domain.aggregate.PropertyUnit;
import com.example.rentalmanager.property.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application Service for the Property bounded context.
 *
 * <p>Orchestrates domain objects, persists state via output ports, and
 * publishes domain events after successful persistence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyApplicationService
        implements CreatePropertyUseCase, AddPropertyUnitUseCase,
                   UpdatePropertyUseCase, GetPropertyUseCase,
                   MarkUnitUnavailableUseCase, MarkUnitAvailableUseCase,
                   GetUnitUnavailabilityUseCase {

    private final PropertyPersistencePort            persistencePort;
    private final UnitPersistencePort                unitPersistencePort;
    private final UnitUnavailabilityPersistencePort  unavailabilityPort;
    private final DomainEventPublisher               eventPublisher;

    // ── CreatePropertyUseCase ──────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<PropertyResponse> createProperty(CreatePropertyCommand cmd) {
        log.debug("Creating property '{}' for owner {}", cmd.name(), cmd.ownerId());
        var address  = new Address(cmd.street(), cmd.city(), cmd.state(), cmd.zipCode(), cmd.country());
        var property = Property.create(OwnerId.of(cmd.ownerId()), cmd.name(), address, cmd.type());
        return persistencePort.save(property)
                .doOnSuccess(this::publishEvents)
                .flatMap(this::buildPropertyResponse);
    }

    // ── AddPropertyUnitUseCase ─────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<PropertyResponse> addUnit(AddPropertyUnitCommand cmd) {
        log.debug("Adding unit '{}' to property {}", cmd.unitNumber(), cmd.propertyId());
        return persistencePort.findById(PropertyId.of(cmd.propertyId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Property not found: " + cmd.propertyId())))
                .flatMap(property -> {
                    var rent = MonthlyRent.of(cmd.monthlyRentAmount(), cmd.currencyCode());
                    var unit = new PropertyUnit(UnitId.generate(), cmd.unitNumber(),
                            cmd.bedrooms(), cmd.bathrooms(), cmd.squareFootage(), rent);
                    property.addUnit(unit);
                    return persistencePort.save(property);
                })
                .doOnSuccess(this::publishEvents)
                .flatMap(this::buildPropertyResponse);
    }

    // ── UpdatePropertyUseCase ──────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<PropertyResponse> updateProperty(UpdatePropertyCommand cmd) {
        return persistencePort.findById(PropertyId.of(cmd.propertyId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Property not found: " + cmd.propertyId())))
                .flatMap(property -> {
                    Address newAddress = cmd.street() != null
                            ? new Address(cmd.street(), cmd.city(), cmd.state(), cmd.zipCode(), cmd.country())
                            : null;
                    property.updateDetails(cmd.name(), newAddress);
                    return persistencePort.save(property);
                })
                .flatMap(this::buildPropertyResponse);
    }

    // ── GetPropertyUseCase ─────────────────────────────────────────────────

    @Override
    public Mono<PropertyResponse> getById(UUID propertyId) {
        return persistencePort.findById(PropertyId.of(propertyId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Property not found: " + propertyId)))
                .flatMap(this::buildPropertyResponse);
    }

    @Override
    public Flux<PropertyResponse> getByOwnerId(UUID ownerId) {
        return persistencePort.findByOwnerId(OwnerId.of(ownerId))
                .flatMap(this::buildPropertyResponse);
    }

    @Override
    public Flux<PropertyResponse> getAll() {
        return persistencePort.findAll().flatMap(this::buildPropertyResponse);
    }

    // ── MarkUnitUnavailableUseCase ─────────────────────────────────────────

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<PropertyUnitResponse> markUnavailable(MarkUnitUnavailableCommand cmd) {
        return unitPersistencePort.findById(cmd.unitId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unit not found: " + cmd.unitId())))
                .flatMap(unit -> {
                    unit.markUnavailable();  // throws if OCCUPIED
                    var record = new UnitUnavailability(UUID.randomUUID(), cmd.unitId(),
                            cmd.reason(), cmd.startDate(), cmd.endDate(), Instant.now());
                    return unitPersistencePort.saveStatus(cmd.unitId(), UnitStatus.UNAVAILABLE)
                            .then(unavailabilityPort.save(record))
                            .then(buildUnitResponse(cmd.unitId()));
                });
    }

    // ── MarkUnitAvailableUseCase ───────────────────────────────────────────

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY_MANAGER')")
    public Mono<PropertyUnitResponse> markAvailable(UUID unitId) {
        return unitPersistencePort.findById(unitId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unit not found: " + unitId)))
                .flatMap(unit -> {
                    unit.markAvailable();   // throws if not UNAVAILABLE
                    return unitPersistencePort.saveStatus(unitId, UnitStatus.AVAILABLE)
                            .then(buildUnitResponse(unitId));
                });
    }

    // ── GetUnitUnavailabilityUseCase ───────────────────────────────────────

    @Override
    public Flux<UnitUnavailabilityResponse> getHistory(UUID unitId) {
        return unavailabilityPort.findByUnitId(unitId).map(this::toUnavailabilityResponse);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void publishEvents(Property property) {
        property.getDomainEvents().forEach(eventPublisher::publish);
        property.clearDomainEvents();
    }

    /**
     * Builds a full {@link PropertyResponse} by fetching unavailability data for every unit
     * in the property.
     */
    private Mono<PropertyResponse> buildPropertyResponse(Property p) {
        return Flux.fromIterable(p.getUnits())
                .flatMap(unit -> unavailabilityPort.findByUnitId(unit.getId().value())
                        .collectList()
                        .map(records -> toUnitResponse(unit, records)))
                .collectList()
                .map(unitResponses -> {
                    Map<String, Long> counts = unitResponses.stream().collect(
                            Collectors.groupingBy(r -> r.status().name(), Collectors.counting()));
                    return new PropertyResponse(
                            p.getId().value(), p.getOwnerId().value(), p.getName(),
                            p.getAddress().street(), p.getAddress().city(),
                            p.getAddress().state(), p.getAddress().zipCode(),
                            p.getAddress().country(), p.getType(),
                            unitResponses, counts, p.getCreatedAt());
                });
    }

    /** Builds a single unit response with its unavailability data. */
    private Mono<PropertyUnitResponse> buildUnitResponse(UUID unitId) {
        return unitPersistencePort.findById(unitId)
                .flatMap(unit -> unavailabilityPort.findByUnitId(unitId)
                        .collectList()
                        .map(records -> toUnitResponse(unit, records)));
    }

    private PropertyUnitResponse toUnitResponse(PropertyUnit u, List<UnitUnavailability> records) {
        List<UnitUnavailabilityResponse> history = records.stream()
                .map(this::toUnavailabilityResponse)
                .toList();

        UnitUnavailabilityResponse current = records.stream()
                .filter(r -> r.endDate() == null || !r.endDate().isBefore(LocalDate.now()))
                .findFirst()
                .map(this::toUnavailabilityResponse)
                .orElse(null);

        return new PropertyUnitResponse(
                u.getId().value(), u.getUnitNumber(),
                u.getBedrooms(), u.getBathrooms(), u.getSquareFootage(),
                u.getMonthlyRent().amount(), u.getMonthlyRent().currency().getCurrencyCode(),
                u.getStatus(), current, history);
    }

    private UnitUnavailabilityResponse toUnavailabilityResponse(UnitUnavailability r) {
        return new UnitUnavailabilityResponse(r.id(), r.reason(), r.startDate(), r.endDate(), r.createdAt());
    }
}
