package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.command.UpdateMaintenanceStatusCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceRequestResponse;
import reactor.core.publisher.Mono;

public interface UpdateMaintenanceRequestUseCase {
    Mono<MaintenanceRequestResponse> updateStatus(UpdateMaintenanceStatusCommand command);
}
