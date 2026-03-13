package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.UpdatePropertyCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyResponse;
import reactor.core.publisher.Mono;

/** Input port for updating mutable property details. */
public interface UpdatePropertyUseCase {

    Mono<PropertyResponse> updateProperty(UpdatePropertyCommand command);
}
