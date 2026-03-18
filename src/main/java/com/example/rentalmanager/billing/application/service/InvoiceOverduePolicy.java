package com.example.rentalmanager.billing.application.service;

import com.example.rentalmanager.billing.application.port.output.InvoicePersistencePort;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceOverduePolicy {

    private final InvoicePersistencePort invoicePersistencePort;
    private final DomainEventPublisher   eventPublisher;

    @Scheduled(cron = "0 0 2 * * *")
    public void markOverdueInvoices() {
        var today = LocalDate.now();
        invoicePersistencePort.findByStatus(InvoiceStatus.PENDING)
                .filter(inv -> inv.getDueDate().isBefore(today))
                .doOnNext(inv -> {
                    try {
                        inv.markOverdue();
                        inv.getDomainEvents().forEach(eventPublisher::publish);
                        inv.clearDomainEvents();
                    } catch (Exception e) {
                        log.warn("Could not mark invoice {} as overdue: {}", inv.getId(), e.getMessage());
                    }
                })
                .flatMap(invoicePersistencePort::save)
                .doOnError(e -> log.error("Error in overdue invoice policy", e))
                .subscribe();
    }
}
