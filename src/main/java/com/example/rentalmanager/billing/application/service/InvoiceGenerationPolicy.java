package com.example.rentalmanager.billing.application.service;

import com.example.rentalmanager.billing.application.port.output.InvoicePersistencePort;
import com.example.rentalmanager.billing.domain.aggregate.Invoice;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceType;
import com.example.rentalmanager.billing.domain.valueobject.Money;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceGenerationPolicy {

    private final InvoicePersistencePort invoicePersistencePort;
    private final DomainEventPublisher   eventPublisher;

    /** Runs at midnight on the 1st of each month to generate RENT invoices for all ACTIVE leases. */
    @Scheduled(cron = "0 0 0 1 * *")
    public void generateInvoices() {
        // This method is called by InvoiceGenerationHandler for specific leases on activation.
        // For the monthly cron, we rely on the leasing context providing active lease details
        // via a query port or direct event replay. For now, log and no-op since
        // InvoiceGenerationHandler covers lease activation.
        log.info("InvoiceGenerationPolicy: monthly invoice generation triggered (1st of month)");
        // Full implementation requires a LeaseQueryPort to list ACTIVE leases.
        // This is a placeholder — wired up when the lease query port is introduced.
    }

    /** Creates a RENT invoice for a specific lease (used by InvoiceGenerationHandler). */
    public Mono<Void> generateRentInvoice(UUID leaseId, UUID tenantId, BigDecimal monthlyRent, LocalDate dueDate) {
        var from = dueDate.withDayOfMonth(1);
        var to   = dueDate.withDayOfMonth(dueDate.lengthOfMonth());

        return invoicePersistencePort.findByLeaseIdAndDueDateBetween(leaseId, from, to)
                .filter(inv -> inv.getType() == InvoiceType.RENT)
                .hasElements()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.debug("RENT invoice for lease {} in {} already exists — skipping", leaseId, dueDate.getMonth());
                        return Mono.empty();
                    }
                    var invoice = Invoice.create(leaseId, tenantId,
                            Money.of(monthlyRent, "KES"), dueDate, InvoiceType.RENT);
                    return invoicePersistencePort.save(invoice)
                            .doOnSuccess(saved -> {
                                saved.getDomainEvents().forEach(eventPublisher::publish);
                                saved.clearDomainEvents();
                            })
                            .then();
                });
    }

    /** Creates a SECURITY_DEPOSIT invoice (used by InvoiceGenerationHandler). */
    public Mono<Void> generateSecurityDepositInvoice(UUID leaseId, UUID tenantId, BigDecimal deposit, LocalDate dueDate) {
        return invoicePersistencePort.findByLeaseId(leaseId)
                .filter(inv -> inv.getType() == InvoiceType.SECURITY_DEPOSIT)
                .hasElements()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.debug("SECURITY_DEPOSIT invoice for lease {} already exists — skipping", leaseId);
                        return Mono.empty();
                    }
                    var invoice = Invoice.create(leaseId, tenantId,
                            Money.of(deposit, "KES"), dueDate, InvoiceType.SECURITY_DEPOSIT);
                    return invoicePersistencePort.save(invoice)
                            .doOnSuccess(saved -> {
                                saved.getDomainEvents().forEach(eventPublisher::publish);
                                saved.clearDomainEvents();
                            })
                            .then();
                });
    }
}
