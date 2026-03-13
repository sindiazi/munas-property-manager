package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.response.PropertyResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Input port for querying properties. */
public interface GetPropertyUseCase {

    Mono<PropertyResponse> getById(UUID propertyId);

    Flux<PropertyResponse> getByOwnerId(UUID ownerId);

    Flux<PropertyResponse> getAll();
}
