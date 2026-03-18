package com.example.rentalmanager.billing.application.port.input;

import com.example.rentalmanager.billing.application.dto.command.CreateInvoiceCommand;
import com.example.rentalmanager.billing.application.dto.response.InvoiceResponse;
import reactor.core.publisher.Mono;

public interface CreateInvoiceUseCase {
    Mono<InvoiceResponse> createInvoice(CreateInvoiceCommand command);
}
