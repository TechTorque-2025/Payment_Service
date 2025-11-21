package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.InvoiceItem;
import com.techtorque.payment_service.entity.InvoiceStatus;
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
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();

        testInvoice = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service456")
                .amount(new BigDecimal("1000.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .notes("Test invoice")
                .requiresDeposit(false)
                .build();
    }

    @Test
    void testSaveInvoice() {
        Invoice saved = invoiceRepository.save(testInvoice);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo("customer123");
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void testFindById() {
        invoiceRepository.save(testInvoice);

        Optional<Invoice> found = invoiceRepository.findById(testInvoice.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getServiceOrProjectId()).isEqualTo("service456");
    }

    @Test
    void testFindByCustomerId() {
        Invoice invoice2 = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service789")
                .amount(new BigDecimal("500.00"))
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .requiresDeposit(false)
                .build();

        invoiceRepository.save(testInvoice);
        invoiceRepository.save(invoice2);

        List<Invoice> invoices = invoiceRepository.findByCustomerId("customer123");

        assertThat(invoices).hasSize(2);
        assertThat(invoices).allMatch(i -> i.getCustomerId().equals("customer123"));
    }

    @Test
    void testFindByCustomerIdOrderByCreatedAtDesc() {
        Invoice invoice2 = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service789")
                .amount(new BigDecimal("500.00"))
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .requiresDeposit(false)
                .build();

        invoiceRepository.save(testInvoice);
        invoiceRepository.save(invoice2);

        List<Invoice> invoices = invoiceRepository.findByCustomerIdOrderByCreatedAtDesc("customer123");

        assertThat(invoices).hasSize(2);
        assertThat(invoices.get(0).getCreatedAt()).isAfterOrEqualTo(invoices.get(1).getCreatedAt());
    }

    @Test
    void testFindByStatus() {
        Invoice invoice2 = Invoice.builder()
                .customerId("customer456")
                .serviceOrProjectId("service789")
                .amount(new BigDecimal("500.00"))
                .status(InvoiceStatus.PAID)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .requiresDeposit(false)
                .build();

        Invoice invoice3 = Invoice.builder()
                .customerId("customer789")
                .serviceOrProjectId("service999")
                .amount(new BigDecimal("750.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(20))
                .requiresDeposit(false)
                .build();

        invoiceRepository.save(testInvoice);
        invoiceRepository.save(invoice2);
        invoiceRepository.save(invoice3);

        List<Invoice> sentInvoices = invoiceRepository.findByStatus(InvoiceStatus.SENT);

        assertThat(sentInvoices).hasSize(2);
        assertThat(sentInvoices).allMatch(i -> i.getStatus() == InvoiceStatus.SENT);
    }

    @Test
    void testFindByServiceOrProjectId() {
        invoiceRepository.save(testInvoice);

        Optional<Invoice> found = invoiceRepository.findByServiceOrProjectId("service456");

        assertThat(found).isPresent();
        assertThat(found.get().getServiceOrProjectId()).isEqualTo("service456");
    }

    @Test
    void testFindByCustomerIdAndStatus() {
        Invoice invoice2 = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service789")
                .amount(new BigDecimal("500.00"))
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .requiresDeposit(false)
                .build();

        Invoice invoice3 = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service999")
                .amount(new BigDecimal("750.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(20))
                .requiresDeposit(false)
                .build();

        invoiceRepository.save(testInvoice);
        invoiceRepository.save(invoice2);
        invoiceRepository.save(invoice3);

        List<Invoice> sentInvoices = invoiceRepository.findByCustomerIdAndStatus(
                "customer123", InvoiceStatus.SENT);

        assertThat(sentInvoices).hasSize(2);
        assertThat(sentInvoices).allMatch(i ->
                i.getCustomerId().equals("customer123") && i.getStatus() == InvoiceStatus.SENT);
    }

    @Test
    void testFindOverdueInvoices() {
        Invoice overdueInvoice = Invoice.builder()
                .customerId("customer456")
                .serviceOrProjectId("service789")
                .amount(new BigDecimal("500.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now().minusDays(60))
                .dueDate(LocalDate.now().minusDays(5))
                .requiresDeposit(false)
                .build();

        invoiceRepository.save(testInvoice);
        invoiceRepository.save(overdueInvoice);

        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(LocalDate.now());

        assertThat(overdueInvoices).hasSize(1);
        assertThat(overdueInvoices.get(0).getDueDate()).isBefore(LocalDate.now());
    }

    @Test
    void testFindOverdueInvoicesByCustomer() {
        Invoice overdueInvoice = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service789")
                .amount(new BigDecimal("500.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now().minusDays(60))
                .dueDate(LocalDate.now().minusDays(5))
                .requiresDeposit(false)
                .build();

        Invoice overdueInvoiceOtherCustomer = Invoice.builder()
                .customerId("customer456")
                .serviceOrProjectId("service999")
                .amount(new BigDecimal("750.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now().minusDays(60))
                .dueDate(LocalDate.now().minusDays(10))
                .requiresDeposit(false)
                .build();

        invoiceRepository.save(testInvoice);
        invoiceRepository.save(overdueInvoice);
        invoiceRepository.save(overdueInvoiceOtherCustomer);

        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoicesByCustomer(
                "customer123", LocalDate.now());

        assertThat(overdueInvoices).hasSize(1);
        assertThat(overdueInvoices.get(0).getCustomerId()).isEqualTo("customer123");
        assertThat(overdueInvoices.get(0).getDueDate()).isBefore(LocalDate.now());
    }

    @Test
    void testFindInvoicesBetweenDates() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().plusDays(30);

        Invoice invoice2 = Invoice.builder()
                .customerId("customer456")
                .serviceOrProjectId("service789")
                .amount(new BigDecimal("500.00"))
                .status(InvoiceStatus.PAID)
                .issueDate(LocalDate.now().minusDays(100))
                .dueDate(LocalDate.now().minusDays(70))
                .requiresDeposit(false)
                .build();

        invoiceRepository.save(testInvoice);
        invoiceRepository.save(invoice2);

        List<Invoice> invoices = invoiceRepository.findInvoicesBetweenDates(startDate, endDate);

        assertThat(invoices).hasSize(1);
        assertThat(invoices.get(0).getIssueDate()).isBetween(startDate, endDate);
    }

    @Test
    void testUpdateInvoice() {
        invoiceRepository.save(testInvoice);

        testInvoice.setStatus(InvoiceStatus.PAID);
        testInvoice.setNotes("Invoice paid in full");
        Invoice updated = invoiceRepository.save(testInvoice);

        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(updated.getNotes()).contains("paid in full");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void testDeleteInvoice() {
        invoiceRepository.save(testInvoice);
        String invoiceId = testInvoice.getId();

        invoiceRepository.deleteById(invoiceId);

        Optional<Invoice> deleted = invoiceRepository.findById(invoiceId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testInvoiceWithItems() {
        InvoiceItem item1 = InvoiceItem.builder()
                .description("Labor - 10 hours")
                .quantity(10)
                .unitPrice(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("500.00"))
                .itemType("LABOR")
                .build();

        InvoiceItem item2 = InvoiceItem.builder()
                .description("Parts")
                .quantity(5)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("500.00"))
                .itemType("PARTS")
                .build();

        testInvoice.addItem(item1);
        testInvoice.addItem(item2);

        Invoice saved = invoiceRepository.save(testInvoice);

        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.calculateTotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void testInvoiceWithDepositRequirement() {
        Invoice depositInvoice = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service456")
                .amount(new BigDecimal("2000.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .requiresDeposit(true)
                .depositAmount(new BigDecimal("1000.00"))
                .depositPaid(new BigDecimal("1000.00"))
                .finalAmount(new BigDecimal("1000.00"))
                .finalPaid(BigDecimal.ZERO)
                .build();

        Invoice saved = invoiceRepository.save(depositInvoice);

        assertThat(saved.getRequiresDeposit()).isTrue();
        assertThat(saved.getDepositAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(saved.getDepositPaid()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void testDefaultValuesOnCreation() {
        Invoice newInvoice = Invoice.builder()
                .customerId("customer999")
                .serviceOrProjectId("service999")
                .amount(new BigDecimal("500.00"))
                .requiresDeposit(false)
                .build();

        Invoice saved = invoiceRepository.save(newInvoice);

        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(saved.getIssueDate()).isNotNull();
        assertThat(saved.getDueDate()).isNotNull();
        assertThat(saved.getDueDate()).isAfter(saved.getIssueDate());
    }

    @Test
    void testPartiallyPaidInvoice() {
        Invoice partiallyPaid = Invoice.builder()
                .customerId("customer123")
                .serviceOrProjectId("service456")
                .amount(new BigDecimal("1000.00"))
                .status(InvoiceStatus.PARTIALLY_PAID)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .requiresDeposit(false)
                .build();

        Invoice saved = invoiceRepository.save(partiallyPaid);

        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }
}
