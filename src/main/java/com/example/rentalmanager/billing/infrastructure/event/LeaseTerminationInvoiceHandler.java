package com.example.rentalmanager.billing.infrastructure.event;

import com.example.rentalmanager.billing.application.port.output.InvoicePersistencePort;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.leasing.domain.event.LeaseExpiredEvent;
import com.example.rentalmanager.leasing.domain.event.LeaseTerminatedEvent;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaseTerminationInvoiceHandler {

    private final InvoicePersistencePort invoicePersistencePort;
    private final DomainEventPublisher   eventPublisher;

    @EventListener
    public void onLeaseTerminated(LeaseTerminatedEvent event) {
        cancelPendingInvoices(event.leaseId().value());
    }

    @EventListener
    public void onLeaseExpired(LeaseExpiredEvent event) {
        cancelPendingInvoices(event.leaseId().value());
    }

    private void cancelPendingInvoices(UUID leaseId) {
        invoicePersistencePort.findByLeaseId(leaseId)
                .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING
                            || inv.getStatus() == InvoiceStatus.OVERDUE)
                .flatMap(inv -> {
                    inv.cancel();
                    inv.getDomainEvents().forEach(eventPublisher::publish);
                    inv.clearDomainEvents();
                    return invoicePersistencePort.save(inv);
                })
                .doOnError(e -> log.error("Error cancelling invoices for lease {}: {}", leaseId, e.getMessage()))
                .subscribe();
    }
}
