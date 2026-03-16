package com.example.rentalmanager.property.application.port.output;

import com.example.rentalmanager.property.domain.aggregate.PropertyUnit;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Output port for individual unit-level persistence operations. */
public interface UnitPersistencePort {

    Mono<PropertyUnit> findById(UUID unitId);

    Mono<PropertyUnit> saveStatus(UUID unitId, UnitStatus newStatus);
}
