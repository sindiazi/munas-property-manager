package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.CreatePropertyCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyResponse;
import reactor.core.publisher.Mono;

/**
 * Input port (primary port) — the entry point into the property domain
 * for the create-property workflow.
 *
 * <p>Implemented by {@code PropertyApplicationService}.
 */
public interface CreatePropertyUseCase {

    Mono<PropertyResponse> createProperty(CreatePropertyCommand command);
}
