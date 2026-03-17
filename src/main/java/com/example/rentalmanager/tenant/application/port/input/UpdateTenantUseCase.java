package com.example.rentalmanager.tenant.application.port.input;

import com.example.rentalmanager.tenant.application.dto.command.UpdateTenantCommand;
import com.example.rentalmanager.tenant.application.dto.response.TenantResponse;
import reactor.core.publisher.Mono;

public interface UpdateTenantUseCase {

    Mono<TenantResponse> update(UpdateTenantCommand cmd);
}
