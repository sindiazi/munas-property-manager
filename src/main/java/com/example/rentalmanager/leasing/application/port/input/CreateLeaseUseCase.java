package com.example.rentalmanager.leasing.application.port.input;

import com.example.rentalmanager.leasing.application.dto.command.CreateLeaseCommand;
import com.example.rentalmanager.leasing.application.dto.response.LeaseResponse;
import reactor.core.publisher.Mono;

/** Input port for drafting a new lease agreement. */
public interface CreateLeaseUseCase {
    Mono<LeaseResponse> createLease(CreateLeaseCommand command);
}
