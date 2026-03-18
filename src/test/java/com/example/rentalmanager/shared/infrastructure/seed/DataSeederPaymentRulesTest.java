package com.example.rentalmanager.shared.infrastructure.seed;

import com.example.rentalmanager.billing.domain.valueobject.InvoiceStatus;
import com.example.rentalmanager.billing.domain.valueobject.InvoiceType;
import com.example.rentalmanager.billing.domain.valueobject.PaymentTransactionStatus;
import com.example.rentalmanager.billing.infrastructure.persistence.entity.InvoiceEntity;
import com.example.rentalmanager.billing.infrastructure.persistence.entity.PaymentEntity;
import com.example.rentalmanager.billing.infrastructure.persistence.repository.InvoiceCassandraRepository;
import com.example.rentalmanager.billing.infrastructure.persistence.repository.PaymentCassandraRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.LeaseR2dbcRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.TenantOccupiedUnitRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.UnitRentalHistoryRepository;
import com.example.rentalmanager.maintenance.infrastructure.persistence.repository.MaintenanceCategoryRepository;
import com.example.rentalmanager.maintenance.infrastructure.persistence.repository.MaintenanceIssueTemplateRepository;
import com.example.rentalmanager.maintenance.infrastructure.persistence.repository.MaintenanceRequestR2dbcRepository;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyR2dbcRepository;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyUnitR2dbcRepository;
import com.example.rentalmanager.shared.infrastructure.security.SsnEncryptionService;
import com.example.rentalmanager.tenant.infrastructure.persistence.repository.TenantR2dbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies the seeder's payment-building rules for every invoice status:
 *
 * <ul>
 *   <li>PAID         → exactly 1 COMPLETED payment, amount == amountDue</li>
 *   <li>PARTIALLY_PAID → exactly 1 COMPLETED payment, amount == amountPaid &lt; amountDue</li>
 *   <li>OVERDUE / CANCELLED / PENDING → no payments</li>
 * </ul>
 */
class DataSeederPaymentRulesTest {

    private DataSeeder seeder;

    private static final BigDecimal AMOUNT_DUE  = new BigDecimal("15000.00");
    private static final BigDecimal AMOUNT_PARTIAL = new BigDecimal("7500.00");
    private static final LocalDate  DUE_DATE    = LocalDate.of(2025, 6, 1);
    private static final LocalDate  PAID_DATE   = LocalDate.of(2025, 5, 28);

    @BeforeEach
    void setUp() {
        seeder = new DataSeeder(
                mock(PropertyR2dbcRepository.class),
                mock(PropertyUnitR2dbcRepository.class),
                mock(TenantR2dbcRepository.class),
                mock(LeaseR2dbcRepository.class),
                mock(InvoiceCassandraRepository.class),
                mock(PaymentCassandraRepository.class),
                mock(MaintenanceRequestR2dbcRepository.class),
                mock(MaintenanceCategoryRepository.class),
                mock(MaintenanceIssueTemplateRepository.class),
                mock(TenantOccupiedUnitRepository.class),
                mock(UnitRentalHistoryRepository.class),
                mock(SsnEncryptionService.class));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private InvoiceEntity invoiceWith(InvoiceStatus status, BigDecimal amountDue, BigDecimal amountPaid) {
        return InvoiceEntity.builder()
                .id(UUID.randomUUID())
                .leaseId(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .amountDue(amountDue)
                .amountPaid(amountPaid)
                .currencyCode("KES")
                .dueDate(DUE_DATE)
                .paidDate(status == InvoiceStatus.PAID || status == InvoiceStatus.PARTIALLY_PAID ? PAID_DATE : null)
                .status(status)
                .type(InvoiceType.RENT)
                .build();
    }

    // ── PAID ───────────────────────────────────────────────────────────────────

    @Test
    void paid_invoice_produces_exactly_one_completed_payment_for_full_amount() {
        InvoiceEntity invoice = invoiceWith(InvoiceStatus.PAID, AMOUNT_DUE, AMOUNT_DUE);

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments).hasSize(1);
        PaymentEntity p = payments.get(0);
        assertThat(p.getStatus()).isEqualTo(PaymentTransactionStatus.COMPLETED);
        assertThat(p.getAmount()).isEqualByComparingTo(AMOUNT_DUE);
        assertThat(p.getInvoiceId()).isEqualTo(invoice.getId());
        assertThat(p.getTenantId()).isEqualTo(invoice.getTenantId());
        assertThat(p.getPaymentDate()).isEqualTo(PAID_DATE);
    }

    @Test
    void paid_invoice_payment_amount_equals_amount_due_not_more_not_less() {
        BigDecimal due = new BigDecimal("22500.00");
        InvoiceEntity invoice = invoiceWith(InvoiceStatus.PAID, due, due);

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getAmount()).isEqualByComparingTo(due);
    }

    // ── PARTIALLY_PAID ─────────────────────────────────────────────────────────

    @Test
    void partially_paid_invoice_produces_exactly_one_completed_payment_for_partial_amount() {
        InvoiceEntity invoice = invoiceWith(InvoiceStatus.PARTIALLY_PAID, AMOUNT_DUE, AMOUNT_PARTIAL);

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments).hasSize(1);
        PaymentEntity p = payments.get(0);
        assertThat(p.getStatus()).isEqualTo(PaymentTransactionStatus.COMPLETED);
        assertThat(p.getAmount()).isEqualByComparingTo(AMOUNT_PARTIAL);
        assertThat(p.getAmount()).isLessThan(invoice.getAmountDue());
    }

    @Test
    void partially_paid_invoice_payment_does_not_equal_amount_due() {
        InvoiceEntity invoice = invoiceWith(InvoiceStatus.PARTIALLY_PAID, AMOUNT_DUE, AMOUNT_PARTIAL);

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments.get(0).getAmount())
                .usingComparator(BigDecimal::compareTo)
                .isNotEqualTo(AMOUNT_DUE);
    }

    // ── No-payment statuses ────────────────────────────────────────────────────

    @ParameterizedTest
    @EnumSource(value = InvoiceStatus.class, names = {"OVERDUE", "CANCELLED", "PENDING"})
    void non_payment_statuses_produce_no_payments(InvoiceStatus status) {
        InvoiceEntity invoice = invoiceWith(status, AMOUNT_DUE, BigDecimal.ZERO);

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments).isEmpty();
    }

    // ── Edge cases ─────────────────────────────────────────────────────────────

    @Test
    void paid_invoice_with_null_paid_date_falls_back_to_due_date() {
        InvoiceEntity invoice = InvoiceEntity.builder()
                .id(UUID.randomUUID())
                .leaseId(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .amountDue(AMOUNT_DUE)
                .amountPaid(AMOUNT_DUE)
                .currencyCode("KES")
                .dueDate(DUE_DATE)
                .paidDate(null)   // ← null paidDate
                .status(InvoiceStatus.PAID)
                .type(InvoiceType.RENT)
                .build();

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getPaymentDate()).isEqualTo(DUE_DATE);
    }

    @Test
    void paid_invoice_with_zero_amount_paid_produces_no_payments() {
        // Guard: should not happen in practice, but the method must be defensive
        InvoiceEntity invoice = invoiceWith(InvoiceStatus.PAID, AMOUNT_DUE, BigDecimal.ZERO);

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments).isEmpty();
    }

    @Test
    void paid_invoice_with_null_amount_paid_produces_no_payments() {
        InvoiceEntity invoice = invoiceWith(InvoiceStatus.PAID, AMOUNT_DUE, null);

        List<PaymentEntity> payments = seeder.buildPaymentsForInvoice(invoice);

        assertThat(payments).isEmpty();
    }
}
