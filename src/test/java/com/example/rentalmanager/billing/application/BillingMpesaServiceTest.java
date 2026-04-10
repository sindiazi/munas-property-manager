package com.example.rentalmanager.billing.application;

import com.example.rentalmanager.billing.application.dto.command.InitiateMpesaPaymentCommand;
import com.example.rentalmanager.billing.application.dto.command.MpesaCallbackCommand;
import com.example.rentalmanager.billing.application.port.output.*;
import com.example.rentalmanager.billing.application.service.BillingApplicationService;
import com.example.rentalmanager.billing.domain.aggregate.Invoice;
import com.example.rentalmanager.billing.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.billing.domain.aggregate.Payment;
import com.example.rentalmanager.billing.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingMpesaServiceTest {

    @Mock private InvoicePersistencePort  invoicePersistencePort;
    @Mock private PaymentPersistencePort  paymentPersistencePort;
    @Mock private PaymentGatewayPort      mpesaGatewayPort;
    @Mock private MpesaTransactionPort    mpesaTransactionPort;
    @Mock private DomainEventPublisher    eventPublisher;

    private BillingApplicationService service;

    @BeforeEach
    void setUp() {
        service = new BillingApplicationService(
                invoicePersistencePort, paymentPersistencePort,
                eventPublisher, mpesaGatewayPort, mpesaTransactionPort);
    }

    // ── initiateMpesaPayment ────────────────────────────────────────────────

    @Test
    void initiate_whenGatewaySucceeds_returnsInitiationResponseAndSavesTransaction() {
        var leaseId   = UUID.randomUUID();
        var tenantId  = UUID.randomUUID();
        var invoiceId = UUID.randomUUID();

        var invoice = Invoice.create(leaseId, tenantId,
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(7), InvoiceType.RENT);

        var savedPayment = Payment.recordMpesa(invoice.getId().value(), tenantId,
                Money.of(new BigDecimal("5000"), "KES"), LocalDate.now());

        var gatewayResult = new GatewayInitiationResult(
                "ws_CO_TEST_123", "MR_TEST_456", "Success. Request accepted.");

        var savedTx = new MpesaTransaction("ws_CO_TEST_123", "MR_TEST_456",
                savedPayment.getId().value(), tenantId, "254708374149",
                new BigDecimal("5000"), MpesaTransactionStatus.INITIATED,
                null, Instant.now(), null);

        var cmd = new InitiateMpesaPaymentCommand(invoice.getId().value(),
                new BigDecimal("5000"), "254708374149");

        when(invoicePersistencePort.findById(any(InvoiceId.class))).thenReturn(Mono.just(invoice));
        when(paymentPersistencePort.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
        when(mpesaGatewayPort.initiate(any(GatewayPaymentRequest.class)))
                .thenReturn(Mono.just(gatewayResult));
        when(mpesaTransactionPort.save(any(MpesaTransaction.class))).thenReturn(Mono.just(savedTx));

        StepVerifier.create(service.initiateMpesaPayment(cmd))
                .assertNext(resp -> {
                    assertThat(resp.checkoutRequestId()).isEqualTo("ws_CO_TEST_123");
                    assertThat(resp.merchantRequestId()).isEqualTo("MR_TEST_456");
                    assertThat(resp.invoiceStatus()).isEqualTo(InvoiceStatus.PENDING);
                })
                .verifyComplete();

        verify(mpesaTransactionPort).save(any(MpesaTransaction.class));
    }

    @Test
    void initiate_whenGatewayRejects_propagatesError() {
        var leaseId  = UUID.randomUUID();
        var tenantId = UUID.randomUUID();

        var invoice = Invoice.create(leaseId, tenantId,
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(7), InvoiceType.RENT);

        var savedPayment = Payment.recordMpesa(invoice.getId().value(), tenantId,
                Money.of(new BigDecimal("5000"), "KES"), LocalDate.now());

        var cmd = new InitiateMpesaPaymentCommand(invoice.getId().value(),
                new BigDecimal("5000"), "254708374149");

        when(invoicePersistencePort.findById(any(InvoiceId.class))).thenReturn(Mono.just(invoice));
        when(paymentPersistencePort.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
        when(mpesaGatewayPort.initiate(any()))
                .thenReturn(Mono.error(new IllegalStateException("M-Pesa initiation failed")));

        StepVerifier.create(service.initiateMpesaPayment(cmd))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void initiate_invoiceNotFound_returnsError() {
        var cmd = new InitiateMpesaPaymentCommand(UUID.randomUUID(),
                new BigDecimal("5000"), "254708374149");

        when(invoicePersistencePort.findById(any(InvoiceId.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.initiateMpesaPayment(cmd))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    // ── handleCallback ──────────────────────────────────────────────────────

    @Test
    void handleCallback_resultCode0_marksInvoicePaidAndUpdatesTransaction() {
        var paymentId = UUID.randomUUID();
        var tenantId  = UUID.randomUUID();
        var leaseId   = UUID.randomUUID();

        var tx = new MpesaTransaction("CK_OK", "MR_OK", paymentId, tenantId,
                "254708374149", new BigDecimal("5000"),
                MpesaTransactionStatus.INITIATED, null, Instant.now(), null);

        var invoice = Invoice.create(leaseId, tenantId,
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(3), InvoiceType.RENT);

        var payment = Payment.recordMpesa(invoice.getId().value(), tenantId,
                Money.of(new BigDecimal("5000"), "KES"), LocalDate.now());

        var cmd = new MpesaCallbackCommand("CK_OK", "MR_OK", 0,
                "The service request is processed successfully.",
                "QJK123ABC", new BigDecimal("5000"), "254708374149", Instant.now());

        when(mpesaTransactionPort.findByCheckoutRequestId("CK_OK")).thenReturn(Mono.just(tx));
        when(paymentPersistencePort.findById(any(PaymentId.class))).thenReturn(Mono.just(payment));
        when(invoicePersistencePort.findById(any(InvoiceId.class))).thenReturn(Mono.just(invoice));
        when(paymentPersistencePort.updateStatus(any(), eq(PaymentTransactionStatus.COMPLETED), eq("QJK123ABC")))
                .thenReturn(Mono.just(payment));
        when(invoicePersistencePort.save(any(Invoice.class))).thenReturn(Mono.just(invoice));
        when(mpesaTransactionPort.updateStatus(eq("CK_OK"), eq(MpesaTransactionStatus.CONFIRMED), eq("QJK123ABC")))
                .thenReturn(Mono.just(tx));

        StepVerifier.create(service.handleCallback(cmd))
                .verifyComplete();

        verify(mpesaTransactionPort).updateStatus("CK_OK", MpesaTransactionStatus.CONFIRMED, "QJK123ABC");
    }

    @Test
    void handleCallback_resultCode1032_marksCancelled() {
        var paymentId = UUID.randomUUID();
        var tenantId  = UUID.randomUUID();
        var leaseId   = UUID.randomUUID();

        var tx = new MpesaTransaction("CK_CANCEL", "MR_CANCEL", paymentId, tenantId,
                "254708374149", new BigDecimal("5000"),
                MpesaTransactionStatus.INITIATED, null, Instant.now(), null);

        var invoice = Invoice.create(leaseId, tenantId,
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(3), InvoiceType.RENT);

        var payment = Payment.recordMpesa(invoice.getId().value(), tenantId,
                Money.of(new BigDecimal("5000"), "KES"), LocalDate.now());

        var cmd = new MpesaCallbackCommand("CK_CANCEL", "MR_CANCEL", 1032,
                "Request cancelled by user.", null, null, null, null);

        when(mpesaTransactionPort.findByCheckoutRequestId("CK_CANCEL")).thenReturn(Mono.just(tx));
        when(paymentPersistencePort.findById(any(PaymentId.class))).thenReturn(Mono.just(payment));
        when(invoicePersistencePort.findById(any(InvoiceId.class))).thenReturn(Mono.just(invoice));
        when(paymentPersistencePort.updateStatus(any(), eq(PaymentTransactionStatus.FAILED), isNull()))
                .thenReturn(Mono.just(payment));
        when(mpesaTransactionPort.updateStatus(eq("CK_CANCEL"), eq(MpesaTransactionStatus.CANCELLED), isNull()))
                .thenReturn(Mono.just(tx));

        StepVerifier.create(service.handleCallback(cmd))
                .verifyComplete();

        verify(mpesaTransactionPort).updateStatus("CK_CANCEL", MpesaTransactionStatus.CANCELLED, null);
    }

    @Test
    void handleCallback_unknownCheckoutRequestId_returnsErrorAfterRetries() {
        var cmd = new MpesaCallbackCommand("UNKNOWN_CK", "MR_X", 0, "OK",
                "REC", BigDecimal.TEN, "254700000000", Instant.now());

        when(mpesaTransactionPort.findByCheckoutRequestId("UNKNOWN_CK"))
                .thenReturn(Mono.empty());

        // repeatWhenEmpty retries 3× with 500 ms delays — advance virtual time past all retries
        StepVerifier.withVirtualTime(() -> service.handleCallback(cmd))
                .thenAwait(Duration.ofSeconds(2))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
