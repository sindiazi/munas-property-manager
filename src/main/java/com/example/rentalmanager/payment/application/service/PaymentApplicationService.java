package com.example.rentalmanager.payment.application.service;

import com.example.rentalmanager.payment.application.dto.command.CreatePaymentCommand;
import com.example.rentalmanager.payment.application.dto.command.ProcessPaymentCommand;
import com.example.rentalmanager.payment.application.dto.response.PaymentResponse;
import com.example.rentalmanager.payment.application.port.input.*;
import com.example.rentalmanager.payment.application.port.output.PaymentPersistencePort;
import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.valueobject.Money;
import com.example.rentalmanager.payment.domain.valueobject.PaymentId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Application Service for the Payment bounded context. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService
        implements CreatePaymentUseCase, ProcessPaymentUseCase, GetPaymentUseCase {

    private final PaymentPersistencePort   persistencePort;
    private final ApplicationEventPublisher eventPublisher;

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
    public Mono<PaymentResponse> getById(UUID paymentId) {
        return persistencePort.findById(PaymentId.of(paymentId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Payment not found: " + paymentId)))
                .map(this::toResponse);
    }

    @Override public Flux<PaymentResponse> getByLeaseId(UUID leaseId)   { return persistencePort.findByLeaseId(leaseId).map(this::toResponse); }
    @Override public Flux<PaymentResponse> getByTenantId(UUID tenantId) { return persistencePort.findByTenantId(tenantId).map(this::toResponse); }

    private void publishAndClear(Payment payment) {
        payment.getDomainEvents().forEach(eventPublisher::publishEvent);
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
