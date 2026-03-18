package com.example.rentalmanager.payment.domain;

import com.example.rentalmanager.payment.domain.aggregate.Payment;
import com.example.rentalmanager.payment.domain.event.MpesaPaymentConfirmedEvent;
import com.example.rentalmanager.payment.domain.event.PaymentReceivedEvent;
import com.example.rentalmanager.payment.domain.valueobject.Money;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import com.example.rentalmanager.payment.domain.valueobject.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/** Pure domain tests — no Spring, no mocks. */
class PaymentMpesaTest {

    private Payment pendingPayment() {
        return Payment.create(UUID.randomUUID(), UUID.randomUUID(),
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(7), PaymentType.RENT);
    }

    @Test
    void receiveViaMpesa_whenPending_transitionsToPaid_andRegistersConfirmedEvent() {
        var payment = pendingPayment();
        payment.clearDomainEvents(); // clear PaymentCreatedEvent

        payment.receiveViaMpesa(
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now(), "QJK123ABC", "254708374149");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

        var events = payment.getDomainEvents();
        assertThat(events).hasSize(2); // PaymentReceivedEvent + MpesaPaymentConfirmedEvent
        assertThat(events).anyMatch(e -> e instanceof PaymentReceivedEvent);
        assertThat(events).anyMatch(e -> e instanceof MpesaPaymentConfirmedEvent confirmed
                && "QJK123ABC".equals(confirmed.mpesaReceiptNumber())
                && "254708374149".equals(confirmed.phoneNumber()));
    }

    @Test
    void receiveViaMpesa_whenAlreadyPaid_throwsIllegalStateException() {
        var payment = pendingPayment();
        payment.receiveViaMpesa(Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now(), "REC1", "254708374149");

        assertThatThrownBy(() ->
                payment.receiveViaMpesa(Money.of(new BigDecimal("100"), "KES"),
                        LocalDate.now(), "REC2", "254708374149"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAID");
    }

    @Test
    void receiveViaMpesa_partialThenFull_accumulatesCorrectly() {
        var payment = pendingPayment();
        payment.clearDomainEvents();

        // First partial
        payment.receiveViaMpesa(Money.of(new BigDecimal("2000"), "KES"),
                LocalDate.now(), "REC1", "254708374149");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_PAID);

        // Second completes the balance
        payment.receiveViaMpesa(Money.of(new BigDecimal("3000"), "KES"),
                LocalDate.now(), "REC2", "254708374149");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getAmountPaid().amount()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    void outstandingBalance_afterFullMpesaPayment_isZero() {
        var payment = pendingPayment();
        payment.receiveViaMpesa(Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now(), "REC1", "254708374149");

        assertThat(payment.outstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
