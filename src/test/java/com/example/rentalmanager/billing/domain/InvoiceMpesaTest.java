package com.example.rentalmanager.billing.domain;

import com.example.rentalmanager.billing.domain.aggregate.Invoice;
import com.example.rentalmanager.billing.domain.event.MpesaPaymentConfirmedEvent;
import com.example.rentalmanager.billing.domain.event.InvoiceSettledEvent;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceType;
import com.example.rentalmanager.billing.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/** Pure domain tests — no Spring, no mocks. */
class InvoiceMpesaTest {

    private Invoice pendingInvoice() {
        return Invoice.create(UUID.randomUUID(), UUID.randomUUID(),
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now().plusDays(7), InvoiceType.RENT);
    }

    @Test
    void receiveViaMpesa_whenPending_transitionsToPaid_andRegistersConfirmedEvent() {
        var invoice = pendingInvoice();
        invoice.clearDomainEvents(); // clear InvoiceCreatedEvent

        invoice.receiveViaMpesa(
                Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now(), "QJK123ABC", "254708374149");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);

        var events = invoice.getDomainEvents();
        assertThat(events).hasSize(2); // InvoiceSettledEvent + MpesaPaymentConfirmedEvent
        assertThat(events).anyMatch(e -> e instanceof InvoiceSettledEvent);
        assertThat(events).anyMatch(e -> e instanceof MpesaPaymentConfirmedEvent confirmed
                && "QJK123ABC".equals(confirmed.mpesaReceiptNumber())
                && "254708374149".equals(confirmed.phoneNumber()));
    }

    @Test
    void receiveViaMpesa_whenAlreadyPaid_throwsIllegalStateException() {
        var invoice = pendingInvoice();
        invoice.receiveViaMpesa(Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now(), "REC1", "254708374149");

        assertThatThrownBy(() ->
                invoice.receiveViaMpesa(Money.of(new BigDecimal("100"), "KES"),
                        LocalDate.now(), "REC2", "254708374149"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAID");
    }

    @Test
    void receiveViaMpesa_partialThenFull_accumulatesCorrectly() {
        var invoice = pendingInvoice();
        invoice.clearDomainEvents();

        // First partial
        invoice.receiveViaMpesa(Money.of(new BigDecimal("2000"), "KES"),
                LocalDate.now(), "REC1", "254708374149");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);

        // Second completes the balance
        invoice.receiveViaMpesa(Money.of(new BigDecimal("3000"), "KES"),
                LocalDate.now(), "REC2", "254708374149");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getAmountPaid().amount()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    void outstandingBalance_afterFullMpesaPayment_isZero() {
        var invoice = pendingInvoice();
        invoice.receiveViaMpesa(Money.of(new BigDecimal("5000"), "KES"),
                LocalDate.now(), "REC1", "254708374149");

        assertThat(invoice.outstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
