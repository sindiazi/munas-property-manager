package com.example.rentalmanager.billing.infrastructure.web.controller;

import com.example.rentalmanager.billing.application.dto.command.CreateInvoiceCommand;
import com.example.rentalmanager.billing.application.dto.command.RecordCashPaymentCommand;
import com.example.rentalmanager.billing.application.dto.response.InvoiceResponse;
import com.example.rentalmanager.billing.application.dto.response.PaymentTransactionResponse;
import com.example.rentalmanager.billing.application.port.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Invoices", description = "Invoice management and payment recording")
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final CreateInvoiceUseCase         createInvoiceUseCase;
    private final GetInvoiceUseCase            getInvoiceUseCase;
    private final RecordCashPaymentUseCase     recordCashPaymentUseCase;
    private final GetPaymentsByInvoiceUseCase  getPaymentsByInvoiceUseCase;

    @Operation(summary = "Create a manual invoice")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceCommand command) {
        return createInvoiceUseCase.createInvoice(command);
    }

    @Operation(summary = "List all invoices")
    @GetMapping
    public Flux<InvoiceResponse> getAll() {
        return getInvoiceUseCase.getAll();
    }

    @GetMapping("/{id}")
    public Mono<InvoiceResponse> getById(@PathVariable UUID id) {
        return getInvoiceUseCase.getById(id);
    }

    @Operation(summary = "Get invoices for a lease")
    @GetMapping("/lease/{leaseId}")
    public Flux<InvoiceResponse> getByLease(@PathVariable UUID leaseId) {
        return getInvoiceUseCase.getByLeaseId(leaseId);
    }

    @Operation(summary = "Get invoices for a tenant")
    @GetMapping("/tenant/{tenantId}")
    public Flux<InvoiceResponse> getByTenant(@PathVariable UUID tenantId) {
        return getInvoiceUseCase.getByTenantId(tenantId);
    }

    @Operation(summary = "Record a cash payment against an invoice")
    @PostMapping("/{id}/payments/cash")
    public Mono<InvoiceResponse> recordCash(
            @PathVariable UUID id,
            @Valid @RequestBody RecordCashPaymentCommand command) {
        var merged = new RecordCashPaymentCommand(id, command.amountPaid(), command.paymentDate());
        return recordCashPaymentUseCase.recordCashPayment(merged);
    }

    @Operation(summary = "Get payment transactions for an invoice")
    @GetMapping("/{id}/payments")
    public Flux<PaymentTransactionResponse> getPayments(@PathVariable UUID id) {
        return getPaymentsByInvoiceUseCase.getByInvoiceId(id);
    }
}
