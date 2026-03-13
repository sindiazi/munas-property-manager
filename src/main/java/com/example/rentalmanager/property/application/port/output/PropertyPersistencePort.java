package com.example.rentalmanager.property.application.port.output;

import com.example.rentalmanager.property.domain.aggregate.Property;
import com.example.rentalmanager.property.domain.valueobject.OwnerId;
import com.example.rentalmanager.property.domain.valueobject.PropertyId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output port (secondary port) — the application layer's abstraction over the
 * persistence mechanism. The infrastructure layer provides the implementation.
 *
 * <p>Mirrors {@link com.example.rentalmanager.property.domain.repository.PropertyRepository}
 * intentionally — some architectures collapse these two; here they are kept
 * separate to allow different contracts (e.g. caching, projections).
 */
public interface PropertyPersistencePort {

    Mono<Property> save(Property property);

    Mono<Property> findById(PropertyId id);

    Flux<Property> findByOwnerId(OwnerId ownerId);

    Flux<Property> findAll();

    Mono<Boolean> existsById(PropertyId id);

    Mono<Void> deleteById(PropertyId id);
}
