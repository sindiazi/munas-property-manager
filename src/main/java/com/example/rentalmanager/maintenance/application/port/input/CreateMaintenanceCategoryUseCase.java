package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.command.CreateMaintenanceCategoryCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceCategoryResponse;
import reactor.core.publisher.Mono;

public interface CreateMaintenanceCategoryUseCase {

    Mono<MaintenanceCategoryResponse> createCategory(CreateMaintenanceCategoryCommand cmd);
}
