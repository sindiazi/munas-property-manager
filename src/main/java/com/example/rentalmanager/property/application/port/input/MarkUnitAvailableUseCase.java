package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.response.PropertyUnitResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MarkUnitAvailableUseCase {
    Mono<PropertyUnitResponse> markAvailable(UUID unitId);
}
