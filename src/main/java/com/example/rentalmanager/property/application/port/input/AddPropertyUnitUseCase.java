package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.AddPropertyUnitCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyResponse;
import reactor.core.publisher.Mono;

/** Input port for adding a rentable unit to an existing property. */
public interface AddPropertyUnitUseCase {

    Mono<PropertyResponse> addUnit(AddPropertyUnitCommand command);
}
