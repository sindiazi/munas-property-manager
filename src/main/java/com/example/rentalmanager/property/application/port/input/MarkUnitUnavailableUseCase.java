package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.command.MarkUnitUnavailableCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyUnitResponse;
import reactor.core.publisher.Mono;

public interface MarkUnitUnavailableUseCase {
    Mono<PropertyUnitResponse> markUnavailable(MarkUnitUnavailableCommand command);
}
