package com.example.rentalmanager.tenant.application.port.input;

import com.example.rentalmanager.tenant.application.dto.command.RegisterTenantCommand;
import com.example.rentalmanager.tenant.application.dto.response.TenantResponse;
import reactor.core.publisher.Mono;

/** Input port for registering a new tenant. */
public interface RegisterTenantUseCase {
    Mono<TenantResponse> register(RegisterTenantCommand command);
}
