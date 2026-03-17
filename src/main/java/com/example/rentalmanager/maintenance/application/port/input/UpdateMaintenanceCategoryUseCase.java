package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.command.UpdateMaintenanceCategoryCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceCategoryResponse;
import reactor.core.publisher.Mono;

public interface UpdateMaintenanceCategoryUseCase {

    Mono<MaintenanceCategoryResponse> updateCategory(UpdateMaintenanceCategoryCommand cmd);
}
