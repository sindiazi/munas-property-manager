package com.example.rentalmanager.billing.infrastructure.event;

import com.example.rentalmanager.billing.application.service.InvoiceGenerationPolicy;
import com.example.rentalmanager.leasing.domain.event.LeaseActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceGenerationHandler {

    private final InvoiceGenerationPolicy invoiceGenerationPolicy;

    @EventListener
    public void onLeaseActivated(LeaseActivatedEvent event) {
        log.info("LeaseActivatedEvent received for lease {} — generating RENT invoice", event.leaseId());

        var dueDate = event.startDate().withDayOfMonth(1);

        invoiceGenerationPolicy.generateRentInvoice(
                        event.leaseId().value(),
                        event.tenantId(),
                        event.monthlyRent(),
                        dueDate)
                .doOnError(e -> log.error("Failed to generate RENT invoice for lease {}: {}",
                        event.leaseId(), e.getMessage()))
                .subscribe();
    }
}
