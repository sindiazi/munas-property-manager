package com.example.rentalmanager.leasing.application.port.input;

import com.example.rentalmanager.leasing.application.dto.command.TerminateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.response.LeaseResponse;
import reactor.core.publisher.Mono;

/** Input port for early-terminating an active lease. */
public interface TerminateLeaseUseCase {
    Mono<LeaseResponse> terminateLease(TerminateLeaseCommand command);
}
