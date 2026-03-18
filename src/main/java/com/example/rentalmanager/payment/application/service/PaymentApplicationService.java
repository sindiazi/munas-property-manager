package com.example.rentalmanager.payment.application.service;

import com.example.rentalmanager.payment.application.dto.command.CreatePaymentCommand;
import com.example.rentalmanager.payment.application.dto.command.InitiateMpesaPaymentCommand;
import com.example.rentalmanager.payment.application.dto.command.MpesaCallbackCommand;
import com.example.rentalmanager.payment.application.dto.command.ProcessPaymentCommand;
import com.example.rentalmanager.payment.application.dto.response.MpesaInitiationResponse;
import com.example.rentalmanager.payment.application.dto.response.MpesaStatusResponse;
import com.example.rentalmanager.payment.application.dto.response.PaymentResponse;
import com.example.rentalmanager.payment.application.port.input.*;
import com.example.rentalmanager.payment.application.port.output.GatewayPaymentRequest;
import com.example.rentalmanager.payment.application.port.output.MpesaTransactionPort;
import com.example.rentalmanager.payment.application.port.output.PaymentGatewayPort;
import com.example.rentalmanager.payment.application.port.output.PaymentPersistencePort;
import com.example.rentalmanager.payment.domain.aggregate.MpesaTransaction;
import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.Money;
import com.example.rentalmanager.payment.domain.valueobject.MpesaTransactionStatus;
import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import com.example.rentalmanager.payment.domain.valueobject.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/** Application Service for the Payment bounded context. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService
        implements CreatePaymentUseCase, ProcessPaymentUseCase, GetPaymentUseCase,
                   InitiateMpesaPaymentUseCase, HandleMpesaCallbackUseCase, QueryMpesaStatusUseCase {

    private final PaymentPersistencePort persistencePort;
    private final DomainEventPublisher   eventPublisher;
    private final PaymentGatewayPort     mpesaGatewayPort;
    private final MpesaTransactionPort   mpesaTransactionPort;

    @Override
    @Transactional
    public Mono<PaymentResponse> createPayment(CreatePaymentCommand cmd) {
        var payment = Payment.create(
                cmd.leaseId(), cmd.tenantId(),
                Money.of(cmd.amount(), cmd.currencyCode()),
                cmd.dueDate(), cmd.type());
        return persistencePort.save(payment)
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public Mono<PaymentResponse> processPayment(ProcessPaymentCommand cmd) {
        return persistencePort.findById(PaymentId.of(cmd.paymentId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Payment not found: " + cmd.paymentId())))
                .flatMap(payment -> {
                    payment.receive(Money.of(cmd.amountPaid(), cmd.currencyCode()), cmd.paymentDate());
                    return persistencePort.save(payment);
                })
                .doOnSuccess(this::publishAndClear)
                .map(this::toResponse);
    }

    @Override
    public Flux<PaymentResponse> getAll() {
        return persistencePort.findAll().map(this::toResponse);
    }

    @Override
    public Mono<PaymentResponse> getById(UUID paymentId) {
        return persistencePort.findById(PaymentId.of(paymentId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Payment not found: " + paymentId)))
                .map(this::toResponse);
    }

    @Override public Flux<PaymentResponse> getByLeaseId(UUID leaseId)   { return persistencePort.findByLeaseId(leaseId).map(this::toResponse); }
    @Override public Flux<PaymentResponse> getByTenantId(UUID tenantId) { return persistencePort.findByTenantId(tenantId).map(this::toResponse); }

    @Override
    @Transactional
    public Mono<MpesaInitiationResponse> initiateMpesaPayment(InitiateMpesaPaymentCommand cmd) {
        PhoneNumber phone = PhoneNumber.of(cmd.phoneNumber());
        Payment payment   = Payment.create(cmd.leaseId(), cmd.tenantId(),
                Money.of(cmd.amount(), "KES"), cmd.dueDate(), cmd.type());
        String accountRef = "LSE-" + cmd.leaseId().toString().replace("-", "").substring(0, 8);

        return persistencePort.save(payment)
                .doOnSuccess(ignored -> publishAndClear(payment))
                .flatMap(saved -> mpesaGatewayPort.initiate(
                        new GatewayPaymentRequest(saved.getId().value(), cmd.leaseId(),
                                phone.value(), cmd.amount(), "KES", accountRef))
                        .flatMap(result -> {
                            var tx = new MpesaTransaction(
                                    result.gatewayTransactionId(), result.merchantRequestId(),
                                    saved.getId().value(), cmd.tenantId(), phone.value(),
                                    cmd.amount(), MpesaTransactionStatus.INITIATED,
                                    null, Instant.now(), null);
                            return mpesaTransactionPort.save(tx)
                                    .thenReturn(new MpesaInitiationResponse(
                                            saved.getId().value(),
                                            result.gatewayTransactionId(),
                                            result.merchantRequestId(),
                                            result.customerMessage(),
                                            PaymentStatus.PENDING));
                        }));
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
                .flatMap(tx -> persistencePort.findById(PaymentId.of(tx.getPaymentId()))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                "Payment not found: " + tx.getPaymentId())))
                        .flatMap(payment -> {
                            if (cmd.resultCode() == 0) {
                                var date = cmd.transactionDate()
                                        .atZone(ZoneId.of("Africa/Nairobi")).toLocalDate();
                                payment.receiveViaMpesa(
                                        Money.of(cmd.amount(), "KES"), date,
                                        cmd.mpesaReceiptNumber(), cmd.phoneNumber());
                                return persistencePort.save(payment)
                                        .flatMap(saved -> mpesaTransactionPort.updateStatus(
                                                cmd.checkoutRequestId(),
                                                MpesaTransactionStatus.CONFIRMED,
                                                cmd.mpesaReceiptNumber()))
                                        .doOnSuccess(ignored -> publishAndClear(payment));
                            } else {
                                var status = cmd.resultCode() == 1032
                                        ? MpesaTransactionStatus.CANCELLED
                                        : MpesaTransactionStatus.FAILED;
                                payment.failedViaMpesa(cmd.resultCode(), cmd.resultDesc());
                                return mpesaTransactionPort.updateStatus(
                                                cmd.checkoutRequestId(), status, null)
                                        .doOnSuccess(ignored -> publishAndClear(payment));
                            }
                        }))
                .then();
    }

    @Override
    public Mono<MpesaStatusResponse> queryStatus(UUID paymentId) {
        return mpesaTransactionPort.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No M-Pesa transaction found for payment: " + paymentId)))
                .flatMap(tx -> Mono.zip(
                        mpesaGatewayPort.queryStatus(tx.getCheckoutRequestId()),
                        persistencePort.findById(PaymentId.of(tx.getPaymentId()))
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Payment not found: " + tx.getPaymentId()))))
                        .map(tuple -> {
                            var gatewayResult = tuple.getT1();
                            var payment       = tuple.getT2();
                            var txStatus = switch (gatewayResult.status()) {
                                case SUCCESS   -> MpesaTransactionStatus.CONFIRMED;
                                case FAILED    -> MpesaTransactionStatus.FAILED;
                                case CANCELLED -> MpesaTransactionStatus.CANCELLED;
                                case PENDING   -> MpesaTransactionStatus.INITIATED;
                            };
                            return new MpesaStatusResponse(
                                    payment.getId().value(),
                                    tx.getCheckoutRequestId(),
                                    txStatus,
                                    gatewayResult.resultDescription(),
                                    gatewayResult.receiptNumber(),
                                    gatewayResult.amountPaid(),
                                    payment.getStatus());
                        }));
    }

    private void publishAndClear(Payment payment) {
        payment.getDomainEvents().forEach(eventPublisher::publish);
        payment.clearDomainEvents();
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId().value(), p.getLeaseId(), p.getTenantId(),
                p.getAmountDue().amount(), p.getAmountPaid().amount(),
                p.outstandingBalance(),
                p.getAmountDue().currency().getCurrencyCode(),
                p.getDueDate(), p.getPaidDate(),
                p.getStatus(), p.getType(), p.getCreatedAt());
    }
}
