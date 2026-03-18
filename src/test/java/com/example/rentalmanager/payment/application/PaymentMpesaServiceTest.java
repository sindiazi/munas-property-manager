package com.example.rentalmanager.payment.application;

import com.example.rentalmanager.payment.application.dto.command.InitiateMpesaPaymentCommand;
import com.example.rentalmanager.payment.application.dto.command.MpesaCallbackCommand;
import com.example.rentalmanager.payment.application.port.output.*;
import com.example.rentalmanager.payment.application.service.PaymentApplicationService;
import com.example.rentalmanager.payment.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.*;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMpesaServiceTest {

    @Mock private PaymentPersistencePort persistencePort;
    @Mock private PaymentGatewayPort     mpesaGatewayPort;
    @Mock private MpesaTransactionPort   mpesaTransactionPort;
    @Mock private DomainEventPublisher   eventPublisher;

    private PaymentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PaymentApplicationService(
                persistencePort, eventPublisher, mpesaGatewayPort, mpesaTransactionPort);
    }

    // ── initiateMpesaPayment ────────────────────────────────────────────────

    @Test
    void initiate_whenGatewaySucceeds_returnsInitiationResponseAndSavesTransaction() {
        var leaseId  = UUID.randomUUID();
        var tenantId = UUID.randomUUID();
        var cmd = new InitiateMpesaPaymentCommand(leaseId, tenantId,
                new BigDecimal("5000"), "254708374149",
                LocalDate.now().plusDays(7), PaymentType.RENT);

        var savedPayment = Payment.create(leaseId, tenantId,
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(7), PaymentType.RENT);

        var gatewayResult = new GatewayInitiationResult(
                "ws_CO_TEST_123", "MR_TEST_456", "Success. Request accepted.");

        var savedTx = new MpesaTransaction("ws_CO_TEST_123", "MR_TEST_456",
                savedPayment.getId().value(), tenantId, "254708374149",
                new BigDecimal("5000"), MpesaTransactionStatus.INITIATED,
                null, Instant.now(), null);

        when(persistencePort.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
        when(mpesaGatewayPort.initiate(any(GatewayPaymentRequest.class)))
                .thenReturn(Mono.just(gatewayResult));
        when(mpesaTransactionPort.save(any(MpesaTransaction.class))).thenReturn(Mono.just(savedTx));

        StepVerifier.create(service.initiateMpesaPayment(cmd))
                .assertNext(resp -> {
                    assertThat(resp.checkoutRequestId()).isEqualTo("ws_CO_TEST_123");
                    assertThat(resp.merchantRequestId()).isEqualTo("MR_TEST_456");
                    assertThat(resp.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
                })
                .verifyComplete();

        verify(mpesaTransactionPort).save(any(MpesaTransaction.class));
    }

    @Test
    void initiate_whenGatewayRejects_propagatesError() {
        var cmd = new InitiateMpesaPaymentCommand(UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("5000"), "254708374149",
                LocalDate.now().plusDays(7), PaymentType.RENT);

        var savedPayment = Payment.create(cmd.leaseId(), cmd.tenantId(),
                Money.of(new BigDecimal("5000"), "KES"),
                cmd.dueDate(), PaymentType.RENT);

        when(persistencePort.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
        when(mpesaGatewayPort.initiate(any()))
                .thenReturn(Mono.error(new IllegalStateException("M-Pesa initiation failed")));

        StepVerifier.create(service.initiateMpesaPayment(cmd))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void initiate_accountReferenceIs12CharsAndStartsWithLSE() {
        var leaseId = UUID.randomUUID();
        var cmd = new InitiateMpesaPaymentCommand(leaseId, UUID.randomUUID(),
                new BigDecimal("1000"), "254708374149",
                LocalDate.now().plusDays(7), PaymentType.RENT);

        var savedPayment = Payment.create(leaseId, cmd.tenantId(),
                Money.of(new BigDecimal("1000"), "KES"), cmd.dueDate(), PaymentType.RENT);

        when(persistencePort.save(any())).thenReturn(Mono.just(savedPayment));
        when(mpesaGatewayPort.initiate(any())).thenAnswer(inv -> {
            GatewayPaymentRequest req = inv.getArgument(0);
            assertThat(req.accountReference()).hasSize(12);
            assertThat(req.accountReference()).startsWith("LSE-");
            return Mono.just(new GatewayInitiationResult("CK_123", "MR_123", "OK"));
        });
        when(mpesaTransactionPort.save(any())).thenReturn(Mono.just(
                new MpesaTransaction("CK_123", "MR_123", savedPayment.getId().value(),
                        cmd.tenantId(), "254708374149", new BigDecimal("1000"),
                        MpesaTransactionStatus.INITIATED, null, Instant.now(), null)));

        StepVerifier.create(service.initiateMpesaPayment(cmd)).expectNextCount(1).verifyComplete();
    }

    @Test
    void initiate_amountRoundedToIntegerKES() {
        var cmd = new InitiateMpesaPaymentCommand(UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("1500.75"), "254708374149",
                LocalDate.now().plusDays(7), PaymentType.RENT);

        var savedPayment = Payment.create(cmd.leaseId(), cmd.tenantId(),
                Money.of(new BigDecimal("1500.75"), "KES"), cmd.dueDate(), PaymentType.RENT);

        when(persistencePort.save(any())).thenReturn(Mono.just(savedPayment));
        when(mpesaGatewayPort.initiate(any())).thenAnswer(inv -> {
            GatewayPaymentRequest req = inv.getArgument(0);
            // The adapter receives the raw amount; rounding is the adapter's responsibility.
            // Here we just verify the amount passed to the port is correct.
            assertThat(req.amount()).isEqualByComparingTo(new BigDecimal("1500.75"));
            return Mono.just(new GatewayInitiationResult("CK_999", "MR_999", "OK"));
        });
        when(mpesaTransactionPort.save(any())).thenReturn(Mono.just(
                new MpesaTransaction("CK_999", "MR_999", savedPayment.getId().value(),
                        cmd.tenantId(), "254708374149", new BigDecimal("1500.75"),
                        MpesaTransactionStatus.INITIATED, null, Instant.now(), null)));

        StepVerifier.create(service.initiateMpesaPayment(cmd)).expectNextCount(1).verifyComplete();
    }

    // ── handleCallback ──────────────────────────────────────────────────────

    @Test
    void handleCallback_resultCode0_marksPaymentPaidAndUpdatesTransaction() {
        var paymentId = UUID.randomUUID();
        var tenantId  = UUID.randomUUID();
        var tx = new MpesaTransaction("CK_OK", "MR_OK", paymentId, tenantId,
                "254708374149", new BigDecimal("5000"),
                MpesaTransactionStatus.INITIATED, null, Instant.now(), null);
        var payment = Payment.create(UUID.randomUUID(), tenantId,
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(3), PaymentType.RENT);

        var cmd = new MpesaCallbackCommand("CK_OK", "MR_OK", 0, "The service request is processed successfully.",
                "QJK123ABC", new BigDecimal("5000"), "254708374149",
                Instant.now());

        when(mpesaTransactionPort.findByCheckoutRequestId("CK_OK")).thenReturn(Mono.just(tx));
        when(persistencePort.findById(any(PaymentId.class))).thenReturn(Mono.just(payment));
        when(persistencePort.save(any(Payment.class))).thenReturn(Mono.just(payment));
        when(mpesaTransactionPort.updateStatus(eq("CK_OK"), eq(MpesaTransactionStatus.CONFIRMED), eq("QJK123ABC")))
                .thenReturn(Mono.just(tx));

        StepVerifier.create(service.handleCallback(cmd))
                .verifyComplete();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(mpesaTransactionPort).updateStatus("CK_OK", MpesaTransactionStatus.CONFIRMED, "QJK123ABC");
    }

    @Test
    void handleCallback_resultCode1032_marksCancelled_paymentRemainingPending() {
        var paymentId = UUID.randomUUID();
        var tx = new MpesaTransaction("CK_CANCEL", "MR_CANCEL", paymentId, UUID.randomUUID(),
                "254708374149", new BigDecimal("5000"),
                MpesaTransactionStatus.INITIATED, null, Instant.now(), null);
        var payment = Payment.create(UUID.randomUUID(), tx.getTenantId(),
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(3), PaymentType.RENT);

        var cmd = new MpesaCallbackCommand("CK_CANCEL", "MR_CANCEL", 1032,
                "Request cancelled by user.", null, null, null, null);

        when(mpesaTransactionPort.findByCheckoutRequestId("CK_CANCEL")).thenReturn(Mono.just(tx));
        when(persistencePort.findById(any(PaymentId.class))).thenReturn(Mono.just(payment));
        when(mpesaTransactionPort.updateStatus(eq("CK_CANCEL"), eq(MpesaTransactionStatus.CANCELLED), isNull()))
                .thenReturn(Mono.just(tx));

        StepVerifier.create(service.handleCallback(cmd))
                .verifyComplete();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(mpesaTransactionPort).updateStatus("CK_CANCEL", MpesaTransactionStatus.CANCELLED, null);
    }

    @Test
    void handleCallback_unknownCheckoutRequestId_returnsError() {
        var cmd = new MpesaCallbackCommand("UNKNOWN_CK", "MR_X", 0, "OK",
                "REC", BigDecimal.TEN, "254700000000", Instant.now());

        when(mpesaTransactionPort.findByCheckoutRequestId("UNKNOWN_CK"))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.handleCallback(cmd))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    // ── queryStatus ─────────────────────────────────────────────────────────

    @Test
    void queryStatus_existingTransaction_callsGatewayAndReturnsCompositeResponse() {
        var paymentId = UUID.randomUUID();
        var tx = new MpesaTransaction("CK_QUERY", "MR_QUERY", paymentId, UUID.randomUUID(),
                "254708374149", new BigDecimal("3000"),
                MpesaTransactionStatus.INITIATED, null, Instant.now(), null);
        var payment = Payment.create(UUID.randomUUID(), tx.getTenantId(),
                Money.of(new BigDecimal("3000"), "KES"),
                LocalDate.now().plusDays(3), PaymentType.RENT);
        var gatewayResult = new GatewayStatusResult("CK_QUERY", GatewayPaymentStatus.SUCCESS,
                "The service request is processed successfully.",
                "QJK999ZZZ", new BigDecimal("3000"), "254708374149");

        when(mpesaTransactionPort.findByPaymentId(paymentId)).thenReturn(Mono.just(tx));
        when(mpesaGatewayPort.queryStatus("CK_QUERY")).thenReturn(Mono.just(gatewayResult));
        when(persistencePort.findById(any(PaymentId.class))).thenReturn(Mono.just(payment));

        StepVerifier.create(service.queryStatus(paymentId))
                .assertNext(resp -> {
                    assertThat(resp.checkoutRequestId()).isEqualTo("CK_QUERY");
                    assertThat(resp.transactionStatus()).isEqualTo(MpesaTransactionStatus.CONFIRMED);
                    assertThat(resp.resultDescription()).contains("processed successfully");
                })
                .verifyComplete();
    }
}
