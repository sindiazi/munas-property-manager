package com.example.rentalmanager.property.application.port.input;

import com.example.rentalmanager.property.application.dto.response.UnitUnavailabilityResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface GetUnitUnavailabilityUseCase {
    Flux<UnitUnavailabilityResponse> getHistory(UUID unitId);
}
