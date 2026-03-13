package com.example.rentalmanager.property.domain.repository;

import com.example.rentalmanager.property.domain.aggregate.Property;
import com.example.rentalmanager.property.domain.valueobject.OwnerId;
import com.example.rentalmanager.property.domain.valueobject.PropertyId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output port (secondary port) — defines persistence operations for
 * the {@link Property} aggregate.
 *
 * <p>The domain layer owns this interface; the infrastructure layer
 * provides the adapter (Spring Data R2DBC implementation).
 */
public interface PropertyRepository {

    Mono<Property> save(Property property);

    Mono<Property> findById(PropertyId id);

    Flux<Property> findByOwnerId(OwnerId ownerId);

    Flux<Property> findAll();

    Mono<Void> deleteById(PropertyId id);

    Mono<Boolean> existsById(PropertyId id);
}
