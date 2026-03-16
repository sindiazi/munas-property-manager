package com.example.rentalmanager.property.domain.aggregate;

import com.example.rentalmanager.property.domain.event.PropertyCreatedEvent;
import com.example.rentalmanager.property.domain.event.PropertyUnitAddedEvent;
import com.example.rentalmanager.property.domain.event.UnitStatusChangedEvent;
import com.example.rentalmanager.property.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root: {@code Property}
 *
 * <p>A property is a physical building or plot owned by a landlord. It contains
 * one or more {@link PropertyUnit}s that can be individually rented out.
 *
 * <p>Invariants enforced by this aggregate:
 * <ul>
 *   <li>Unit numbers must be unique within a property.</li>
 *   <li>A property must always have a valid address and owner.</li>
 *   <li>Units can only transition to {@code OCCUPIED} from {@code AVAILABLE} or
 *       {@code RESERVED}.</li>
 * </ul>
 */
public class Property extends AggregateRoot<PropertyId> {

    private final PropertyId id;
    private final OwnerId ownerId;
    private String name;
    private Address address;
    private PropertyType type;
    private final List<PropertyUnit> units;
    private final Instant createdAt;

    @Override public PropertyId  getId()      { return id; }
    public OwnerId               getOwnerId() { return ownerId; }
    public String                getName()    { return name; }
    public Address               getAddress() { return address; }
    public PropertyType          getType()    { return type; }
    public Instant               getCreatedAt() { return createdAt; }

    /**
     * Reconstitution constructor – used by the persistence layer to restore
     * a fully hydrated aggregate from the database.
     */
    public Property(
            PropertyId id,
            OwnerId ownerId,
            String name,
            Address address,
            PropertyType type,
            List<PropertyUnit> units,
            Instant createdAt) {

        this.id        = id;
        this.ownerId   = ownerId;
        this.name      = name;
        this.address   = address;
        this.type      = type;
        this.units     = new ArrayList<>(units);
        this.createdAt = createdAt;
    }

    // ── Factory ────────────────────────────────────────────────────────────

    /**
     * Creates a brand-new property and raises a {@link PropertyCreatedEvent}.
     */
    public static Property create(
            OwnerId ownerId,
            String name,
            Address address,
            PropertyType type) {

        var id       = PropertyId.generate();
        var property = new Property(id, ownerId, name, address, type, List.of(), Instant.now());
        property.registerEvent(new PropertyCreatedEvent(UUID.randomUUID(), Instant.now(), id, ownerId, name));
        return property;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    /**
     * Adds a new rentable unit to this property.
     *
     * @throws IllegalArgumentException if the unit number is already taken
     */
    public void addUnit(PropertyUnit unit) {
        boolean duplicate = units.stream()
                .anyMatch(u -> u.getUnitNumber().equalsIgnoreCase(unit.getUnitNumber()));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "Unit number '%s' already exists in property %s".formatted(unit.getUnitNumber(), id));
        }
        units.add(unit);
        registerEvent(new PropertyUnitAddedEvent(UUID.randomUUID(), Instant.now(), id, unit.getId()));
    }

    /**
     * Marks the specified unit as occupied. Raises {@link UnitStatusChangedEvent}.
     */
    public void occupyUnit(UnitId unitId) {
        var unit = findUnitOrThrow(unitId);
        var previousStatus = unit.getStatus();
        unit.occupy();
        registerEvent(new UnitStatusChangedEvent(UUID.randomUUID(), Instant.now(), id, unitId,
                previousStatus, UnitStatus.OCCUPIED));
    }

    /** Marks the specified unit as available after a lease ends. */
    public void vacateUnit(UnitId unitId) {
        var unit = findUnitOrThrow(unitId);
        var previousStatus = unit.getStatus();
        unit.vacate();
        registerEvent(new UnitStatusChangedEvent(UUID.randomUUID(), Instant.now(), id, unitId,
                previousStatus, UnitStatus.AVAILABLE));
    }

    /** Puts the specified unit into maintenance. */
    public void startUnitMaintenance(UnitId unitId) {
        var unit = findUnitOrThrow(unitId);
        var previousStatus = unit.getStatus();
        unit.startMaintenance();
        registerEvent(new UnitStatusChangedEvent(UUID.randomUUID(), Instant.now(), id, unitId,
                previousStatus, UnitStatus.UNDER_MAINTENANCE));
    }

    /** Completes maintenance and returns the unit to available. */
    public void completeUnitMaintenance(UnitId unitId) {
        var unit = findUnitOrThrow(unitId);
        var previousStatus = unit.getStatus();
        unit.completeMaintenance();
        registerEvent(new UnitStatusChangedEvent(UUID.randomUUID(), Instant.now(), id, unitId,
                previousStatus, UnitStatus.AVAILABLE));
    }

    /** Updates address and/or property name. */
    public void updateDetails(String newName, Address newAddress) {
        if (newName != null && !newName.isBlank()) this.name    = newName;
        if (newAddress != null)                    this.address = newAddress;
    }

    /** Returns an unmodifiable view of the property's units. */
    public List<PropertyUnit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private PropertyUnit findUnitOrThrow(UnitId unitId) {
        return units.stream()
                .filter(u -> u.getId().equals(unitId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unit %s not found in property %s".formatted(unitId, id)));
    }
}
