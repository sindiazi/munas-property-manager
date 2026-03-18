package com.example.rentalmanager.billing.application.service;

import com.example.rentalmanager.billing.application.dto.command.*;
import com.example.rentalmanager.billing.application.dto.response.*;
import com.example.rentalmanager.billing.application.port.input.*;
import com.example.rentalmanager.billing.application.port.output.*;
import com.example.rentalmanager.billing.domain.aggregate.Invoice;
import com.example.rentalmanager.billing.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.billing.domain.aggregate.Payment;
import com.example.rentalmanager.billing.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingApplicationService
        implements CreateInvoiceUseCase, GetInvoiceUseCase,
                   RecordCashPaymentUseCase, GetPaymentsByInvoiceUseCase,
                   InitiateMpesaPaymentUseCase, HandleMpesaCallbackUseCase, QueryMpesaStatusUseCase {

    private final InvoicePersistencePort  invoicePersistencePort;
    private final PaymentPersistencePort  paymentPersistencePort;
    private final DomainEventPublisher    eventPublisher;
    private final PaymentGatewayPort      mpesaGatewayPort;
    private final MpesaTransactionPort    mpesaTransactionPort;

    @Override
    @Transactional
    public Mono<InvoiceResponse> createInvoice(CreateInvoiceCommand cmd) {
        var invoice = Invoice.create(
                cmd.leaseId(), cmd.tenantId(),
                Money.of(cmd.amount(), cmd.currencyCode()),
                cmd.dueDate(), cmd.type());
        return invoicePersistencePort.save(invoice)
                .doOnSuccess(this::publishAndClearInvoice)
                .map(this::toInvoiceResponse);
    }

    @Override
    public Flux<InvoiceResponse> getAll() {
        return invoicePersistencePort.findAll().map(this::toInvoiceResponse);
    }

    @Override
    public Mono<InvoiceResponse> getById(UUID invoiceId) {
        return invoicePersistencePort.findById(InvoiceId.of(invoiceId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invoice not found: " + invoiceId)))
                .map(this::toInvoiceResponse);
    }

    @Override public Flux<InvoiceResponse> getByLeaseId(UUID leaseId)   { return invoicePersistencePort.findByLeaseId(leaseId).map(this::toInvoiceResponse); }
    @Override public Flux<InvoiceResponse> getByTenantId(UUID tenantId) { return invoicePersistencePort.findByTenantId(tenantId).map(this::toInvoiceResponse); }

    @Override
    public Flux<PaymentTransactionResponse> getByInvoiceId(UUID invoiceId) {
        return paymentPersistencePort.findByInvoiceId(invoiceId).map(this::toPaymentResponse);
    }

    @Override
    @Transactional
    public Mono<InvoiceResponse> recordCashPayment(RecordCashPaymentCommand cmd) {
        return invoicePersistencePort.findById(InvoiceId.of(cmd.invoiceId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invoice not found: " + cmd.invoiceId())))
                .flatMap(invoice -> {
                    // Derive currency from invoice
                    var amount = Money.of(cmd.amountPaid(), invoice.getAmountDue().currency().getCurrencyCode());
                    var cashPayment = Payment.recordCash(invoice.getId().value(), invoice.getTenantId(), amount, cmd.paymentDate());

                    invoice.receive(amount, cmd.paymentDate());

                    return paymentPersistencePort.save(cashPayment)
                            .flatMap(savedPayment -> invoicePersistencePort.save(invoice)
                                    .doOnSuccess(savedInvoice -> {
                                        publishAndClearPayment(cashPayment);
                                        publishAndClearInvoice(savedInvoice);
                                    }));
                })
                .map(this::toInvoiceResponse);
    }

    @Override
    @Transactional
    public Mono<MpesaInitiationResponse> initiateMpesaPayment(InitiateMpesaPaymentCommand cmd) {
        return invoicePersistencePort.findById(InvoiceId.of(cmd.invoiceId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invoice not found: " + cmd.invoiceId())))
                .flatMap(invoice -> {
                    PhoneNumber phone       = PhoneNumber.of(cmd.phoneNumber());
                    var         amount      = Money.of(cmd.amount(), "KES");
                    var         mpesaPmt    = Payment.recordMpesa(invoice.getId().value(), invoice.getTenantId(), amount, LocalDate.now());
                    String      accountRef  = "INV-" + invoice.getId().value().toString().replace("-", "").substring(0, 8);

                    return paymentPersistencePort.save(mpesaPmt)
                            .doOnSuccess(ignored -> publishAndClearPayment(mpesaPmt))
                            .flatMap(savedPmt -> mpesaGatewayPort.initiate(
                                    new GatewayPaymentRequest(savedPmt.getId().value(), invoice.getId().value(),
                                            phone.value(), cmd.amount(), "KES", accountRef))
                                    .flatMap(result -> {
                                        var tx = new MpesaTransaction(
                                                result.gatewayTransactionId(), result.merchantRequestId(),
                                                savedPmt.getId().value(), invoice.getTenantId(), phone.value(),
                                                cmd.amount(), MpesaTransactionStatus.INITIATED,
                                                null, Instant.now(), null);
                                        return mpesaTransactionPort.save(tx)
                                                .thenReturn(new MpesaInitiationResponse(
                                                        invoice.getId().value(),
                                                        savedPmt.getId().value(),
                                                        result.gatewayTransactionId(),
                                                        result.merchantRequestId(),
                                                        result.customerMessage(),
                                                        invoice.getStatus()));
                                    }));
                });
    }

    @Override
    @Transactional
    public Mono<Void> handleCallback(MpesaCallbackCommand cmd) {
        return mpesaTransactionPort.findByCheckoutRequestId(cmd.checkoutRequestId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("M-Pesa callback received for unknown CheckoutRequestID '{}' — " +
                             "transaction row not yet persisted; Daraja will retry",
                             cmd.checkoutRequestId());
                    return Mono.error(new IllegalArgumentException(
                            "Unknown M-Pesa transaction: " + cmd.checkoutRequestId()));
                }))
                .flatMap(tx -> paymentPersistencePort.findById(PaymentId.of(tx.getPaymentTransactionId()))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                "Payment transaction not found: " + tx.getPaymentTransactionId())))
                        .flatMap(pmt -> invoicePersistencePort.findById(InvoiceId.of(pmt.getInvoiceId()))
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Invoice not found: " + pmt.getInvoiceId())))
                                .flatMap(invoice -> {
                                    if (cmd.resultCode() == 0) {
                                        var date = cmd.transactionDate()
                                                .atZone(ZoneId.of("Africa/Nairobi")).toLocalDate();
                                        // Mark payment transaction completed
                                        return paymentPersistencePort.updateStatus(
                                                        pmt.getId().value(), PaymentTransactionStatus.COMPLETED, cmd.mpesaReceiptNumber())
                                                .flatMap(updatedPmt -> {
                                                    invoice.receiveViaMpesa(
                                                            Money.of(cmd.amount(), "KES"), date,
                                                            cmd.mpesaReceiptNumber(), cmd.phoneNumber());
                                                    return invoicePersistencePort.save(invoice)
                                                            .flatMap(savedInvoice -> mpesaTransactionPort.updateStatus(
                                                                    cmd.checkoutRequestId(),
                                                                    MpesaTransactionStatus.CONFIRMED,
                                                                    cmd.mpesaReceiptNumber()))
                                                            .doOnSuccess(ignored -> publishAndClearInvoice(invoice));
                                                });
                                    } else {
                                        var status = cmd.resultCode() == 1032
                                                ? MpesaTransactionStatus.CANCELLED
                                                : MpesaTransactionStatus.FAILED;
                                        invoice.failedViaMpesa(cmd.resultCode(), cmd.resultDesc());
                                        return paymentPersistencePort.updateStatus(
                                                        pmt.getId().value(), PaymentTransactionStatus.FAILED, null)
                                                .flatMap(ignored -> mpesaTransactionPort.updateStatus(
                                                        cmd.checkoutRequestId(), status, null))
                                                .doOnSuccess(ignored -> publishAndClearInvoice(invoice));
                                    }
                                })))
                .then();
    }

    @Override
    public Mono<MpesaStatusResponse> queryStatus(UUID paymentTransactionId) {
        return mpesaTransactionPort.findByPaymentTransactionId(paymentTransactionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No M-Pesa transaction found for payment: " + paymentTransactionId)))
                .flatMap(tx -> Mono.zip(
                        mpesaGatewayPort.queryStatus(tx.getCheckoutRequestId()),
                        paymentPersistencePort.findById(PaymentId.of(tx.getPaymentTransactionId()))
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Payment transaction not found: " + tx.getPaymentTransactionId())))
                                .flatMap(pmt -> invoicePersistencePort.findById(InvoiceId.of(pmt.getInvoiceId()))
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                                "Invoice not found: " + pmt.getInvoiceId())))))
                        .map(tuple -> {
                            var gatewayResult = tuple.getT1();
                            var invoice       = tuple.getT2();
                            var txStatus = switch (gatewayResult.status()) {
                                case SUCCESS   -> MpesaTransactionStatus.CONFIRMED;
                                case FAILED    -> MpesaTransactionStatus.FAILED;
                                case CANCELLED -> MpesaTransactionStatus.CANCELLED;
                                case PENDING   -> MpesaTransactionStatus.INITIATED;
                            };
                            return new MpesaStatusResponse(
                                    invoice.getId().value(),
                                    tx.getCheckoutRequestId(),
                                    txStatus,
                                    gatewayResult.resultDescription(),
                                    gatewayResult.receiptNumber(),
                                    gatewayResult.amountPaid(),
                                    invoice.getStatus());
                        }));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private void publishAndClearInvoice(Invoice invoice) {
        invoice.getDomainEvents().forEach(eventPublisher::publish);
        invoice.clearDomainEvents();
    }

    private void publishAndClearPayment(Payment payment) {
        payment.getDomainEvents().forEach(eventPublisher::publish);
        payment.clearDomainEvents();
    }

    private InvoiceResponse toInvoiceResponse(Invoice inv) {
        return new InvoiceResponse(
                inv.getId().value(), inv.getLeaseId(), inv.getTenantId(),
                inv.getAmountDue().amount(), inv.getAmountPaid().amount(),
                inv.outstandingBalance(),
                inv.getAmountDue().currency().getCurrencyCode(),
                inv.getDueDate(), inv.getPaidDate(),
                inv.getStatus(), inv.getType(), inv.getCreatedAt());
    }

    private PaymentTransactionResponse toPaymentResponse(Payment p) {
        return new PaymentTransactionResponse(
                p.getId().value(), p.getInvoiceId(), p.getTenantId(),
                p.getAmount().amount(),
                p.getAmount().currency().getCurrencyCode(),
                p.getMethod(), p.getStatus(), p.getReference(),
                p.getPaymentDate(), p.getCreatedAt());
    }
}
