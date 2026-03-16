package com.example.rentalmanager.payment.infrastructure.web.controller;

import com.example.rentalmanager.payment.application.dto.command.CreatePaymentCommand;
import com.example.rentalmanager.payment.application.dto.command.ProcessPaymentCommand;
import com.example.rentalmanager.payment.application.dto.response.PaymentResponse;
import com.example.rentalmanager.payment.application.port.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Primary adapter exposing Payment operations over HTTP / WebFlux. */
@Tag(name = "Payments", description = "Rent payment tracking and processing")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CreatePaymentUseCase  createPaymentUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final GetPaymentUseCase     getPaymentUseCase;

    @Operation(summary = "Create a payment obligation")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PaymentResponse> create(@Valid @RequestBody CreatePaymentCommand command) {
        return createPaymentUseCase.createPayment(command);
    }

    @Operation(summary = "Record receipt of a payment")
    @PatchMapping("/{id}/receive")
    public Mono<PaymentResponse> receive(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessPaymentCommand command) {
        var merged = new ProcessPaymentCommand(id, command.amountPaid(),
                command.currencyCode(), command.paymentDate());
        return processPaymentUseCase.processPayment(merged);
    }

    @Operation(summary = "List all payments")
    @GetMapping
    public Flux<PaymentResponse> getAll() {
        return getPaymentUseCase.getAll();
    }

    @GetMapping("/{id}")
    public Mono<PaymentResponse> getById(@PathVariable UUID id) {
        return getPaymentUseCase.getById(id);
    }

    @Operation(summary = "Get payments for a lease")
    @GetMapping("/lease/{leaseId}")
    public Flux<PaymentResponse> getByLease(@PathVariable UUID leaseId) {
        return getPaymentUseCase.getByLeaseId(leaseId);
    }

    @Operation(summary = "Get payments for a tenant")
    @GetMapping("/tenant/{tenantId}")
    public Flux<PaymentResponse> getByTenant(@PathVariable UUID tenantId) {
        return getPaymentUseCase.getByTenantId(tenantId);
    }
}
