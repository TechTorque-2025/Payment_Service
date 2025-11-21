package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.ScheduledPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduledPaymentRepositoryTest {

    @Autowired
    private ScheduledPaymentRepository scheduledPaymentRepository;

    private ScheduledPayment testScheduledPayment;

    @BeforeEach
    void setUp() {
        scheduledPaymentRepository.deleteAll();

        testScheduledPayment = ScheduledPayment.builder()
                .invoiceId("invoice123")
                .customerId("customer123")
                .amount(new BigDecimal("500.00"))
                .scheduledDate(LocalDate.now().plusDays(7))
                .status("SCHEDULED")
                .notes("Test scheduled payment")
                .build();
    }

    @Test
    void testSaveScheduledPayment() {
        ScheduledPayment saved = scheduledPaymentRepository.save(testScheduledPayment);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getInvoiceId()).isEqualTo("invoice123");
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(saved.getStatus()).isEqualTo("SCHEDULED");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void testFindById() {
        scheduledPaymentRepository.save(testScheduledPayment);

        Optional<ScheduledPayment> found = scheduledPaymentRepository.findById(testScheduledPayment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getInvoiceId()).isEqualTo("invoice123");
    }

    @Test
    void testFindByCustomerId() {
        ScheduledPayment payment2 = ScheduledPayment.builder()
                .invoiceId("invoice456")
                .customerId("customer123")
                .amount(new BigDecimal("300.00"))
                .scheduledDate(LocalDate.now().plusDays(14))
                .status("SCHEDULED")
                .build();

        scheduledPaymentRepository.save(testScheduledPayment);
        scheduledPaymentRepository.save(payment2);

        List<ScheduledPayment> payments = scheduledPaymentRepository.findByCustomerId("customer123");

        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getCustomerId().equals("customer123"));
    }

    @Test
    void testFindByInvoiceId() {
        ScheduledPayment payment2 = ScheduledPayment.builder()
                .invoiceId("invoice123")
                .customerId("customer456")
                .amount(new BigDecimal("200.00"))
                .scheduledDate(LocalDate.now().plusDays(7))
                .status("SCHEDULED")
                .build();

        scheduledPaymentRepository.save(testScheduledPayment);
        scheduledPaymentRepository.save(payment2);

        List<ScheduledPayment> payments = scheduledPaymentRepository.findByInvoiceId("invoice123");

        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getInvoiceId().equals("invoice123"));
    }

    @Test
    void testFindByStatus() {
        ScheduledPayment payment2 = ScheduledPayment.builder()
                .invoiceId("invoice456")
                .customerId("customer456")
                .amount(new BigDecimal("300.00"))
                .scheduledDate(LocalDate.now().plusDays(14))
                .status("PROCESSED")
                .paymentId("payment123")
                .build();

        ScheduledPayment payment3 = ScheduledPayment.builder()
                .invoiceId("invoice789")
                .customerId("customer789")
                .amount(new BigDecimal("400.00"))
                .scheduledDate(LocalDate.now().plusDays(21))
                .status("SCHEDULED")
                .build();

        scheduledPaymentRepository.save(testScheduledPayment);
        scheduledPaymentRepository.save(payment2);
        scheduledPaymentRepository.save(payment3);

        List<ScheduledPayment> scheduledPayments = scheduledPaymentRepository.findByStatus("SCHEDULED");

        assertThat(scheduledPayments).hasSize(2);
        assertThat(scheduledPayments).allMatch(p -> p.getStatus().equals("SCHEDULED"));
    }

    @Test
    void testFindScheduledPaymentsForDate() {
        LocalDate targetDate = LocalDate.now().plusDays(7);

        ScheduledPayment payment2 = ScheduledPayment.builder()
                .invoiceId("invoice456")
                .customerId("customer456")
                .amount(new BigDecimal("300.00"))
                .scheduledDate(targetDate)
                .status("SCHEDULED")
                .build();

        ScheduledPayment payment3 = ScheduledPayment.builder()
                .invoiceId("invoice789")
                .customerId("customer789")
                .amount(new BigDecimal("400.00"))
                .scheduledDate(LocalDate.now().plusDays(14))
                .status("SCHEDULED")
                .build();

        scheduledPaymentRepository.save(testScheduledPayment);
        scheduledPaymentRepository.save(payment2);
        scheduledPaymentRepository.save(payment3);

        List<ScheduledPayment> payments = scheduledPaymentRepository.findScheduledPaymentsForDate(targetDate);

        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getScheduledDate().equals(targetDate));
        assertThat(payments).allMatch(p -> p.getStatus().equals("SCHEDULED"));
    }

    @Test
    void testFindOverdueScheduledPayments() {
        ScheduledPayment overduePayment = ScheduledPayment.builder()
                .invoiceId("invoice456")
                .customerId("customer456")
                .amount(new BigDecimal("300.00"))
                .scheduledDate(LocalDate.now().minusDays(5))
                .status("SCHEDULED")
                .build();

        ScheduledPayment futurePayment = ScheduledPayment.builder()
                .invoiceId("invoice789")
                .customerId("customer789")
                .amount(new BigDecimal("400.00"))
                .scheduledDate(LocalDate.now().plusDays(14))
                .status("SCHEDULED")
                .build();

        scheduledPaymentRepository.save(overduePayment);
        scheduledPaymentRepository.save(futurePayment);

        List<ScheduledPayment> overduePayments = scheduledPaymentRepository
                .findOverdueScheduledPayments(LocalDate.now());

        assertThat(overduePayments).hasSize(1);
        assertThat(overduePayments.get(0).getScheduledDate()).isBeforeOrEqualTo(LocalDate.now());
    }

    @Test
    void testUpdateScheduledPayment() {
        scheduledPaymentRepository.save(testScheduledPayment);

        testScheduledPayment.setStatus("PROCESSED");
        testScheduledPayment.setPaymentId("payment123");
        testScheduledPayment.setNotes("Payment processed successfully");
        ScheduledPayment updated = scheduledPaymentRepository.save(testScheduledPayment);

        assertThat(updated.getStatus()).isEqualTo("PROCESSED");
        assertThat(updated.getPaymentId()).isEqualTo("payment123");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void testDeleteScheduledPayment() {
        scheduledPaymentRepository.save(testScheduledPayment);
        String paymentId = testScheduledPayment.getId();

        scheduledPaymentRepository.deleteById(paymentId);

        Optional<ScheduledPayment> deleted = scheduledPaymentRepository.findById(paymentId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testCancelScheduledPayment() {
        scheduledPaymentRepository.save(testScheduledPayment);

        testScheduledPayment.setStatus("CANCELLED");
        testScheduledPayment.setNotes("Payment cancelled by customer");
        ScheduledPayment updated = scheduledPaymentRepository.save(testScheduledPayment);

        assertThat(updated.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void testFailedScheduledPayment() {
        scheduledPaymentRepository.save(testScheduledPayment);

        testScheduledPayment.setStatus("FAILED");
        testScheduledPayment.setNotes("Payment failed - insufficient funds");
        ScheduledPayment updated = scheduledPaymentRepository.save(testScheduledPayment);

        assertThat(updated.getStatus()).isEqualTo("FAILED");
        assertThat(updated.getNotes()).contains("insufficient funds");
    }

    @Test
    void testDefaultStatusOnCreation() {
        ScheduledPayment newPayment = ScheduledPayment.builder()
                .invoiceId("invoice999")
                .customerId("customer999")
                .amount(new BigDecimal("100.00"))
                .scheduledDate(LocalDate.now().plusDays(30))
                .build();

        ScheduledPayment saved = scheduledPaymentRepository.save(newPayment);

        assertThat(saved.getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    void testMultiplePaymentsForSameInvoice() {
        ScheduledPayment payment1 = ScheduledPayment.builder()
                .invoiceId("invoice123")
                .customerId("customer123")
                .amount(new BigDecimal("250.00"))
                .scheduledDate(LocalDate.now().plusDays(7))
                .status("SCHEDULED")
                .notes("First installment")
                .build();

        ScheduledPayment payment2 = ScheduledPayment.builder()
                .invoiceId("invoice123")
                .customerId("customer123")
                .amount(new BigDecimal("250.00"))
                .scheduledDate(LocalDate.now().plusDays(14))
                .status("SCHEDULED")
                .notes("Second installment")
                .build();

        scheduledPaymentRepository.save(payment1);
        scheduledPaymentRepository.save(payment2);

        List<ScheduledPayment> invoicePayments = scheduledPaymentRepository.findByInvoiceId("invoice123");

        assertThat(invoicePayments).hasSize(2);
        assertThat(invoicePayments.stream()
                .map(ScheduledPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo(new BigDecimal("500.00"));
    }
}
