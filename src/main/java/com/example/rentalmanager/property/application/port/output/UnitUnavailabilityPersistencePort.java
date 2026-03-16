package com.example.rentalmanager.property.application.port.output;

import com.example.rentalmanager.property.domain.valueobject.UnitUnavailability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Output port for unit unavailability persistence operations. */
public interface UnitUnavailabilityPersistencePort {

    Mono<UnitUnavailability> save(UnitUnavailability unavailability);

    /** Returns all records for a unit ordered by createdAt DESC. */
    Flux<UnitUnavailability> findByUnitId(UUID unitId);
}
