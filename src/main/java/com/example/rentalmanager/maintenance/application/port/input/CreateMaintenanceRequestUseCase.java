package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.command.CreateMaintenanceRequestCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceRequestResponse;
import reactor.core.publisher.Mono;

public interface CreateMaintenanceRequestUseCase {
    Mono<MaintenanceRequestResponse> create(CreateMaintenanceRequestCommand command);
}
