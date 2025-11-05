package com.techtorque.payment_service.config;

import com.techtorque.payment_service.entity.*;
import com.techtorque.payment_service.repository.InvoiceItemRepository;
import com.techtorque.payment_service.repository.InvoiceRepository;
import com.techtorque.payment_service.repository.PaymentRepository;
import com.techtorque.payment_service.repository.ScheduledPaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data seeder for Payment Service - seeds sample invoices, payments, and scheduled payments
 * Only runs in 'dev' profile to avoid polluting production data
 */
@Configuration
@Profile("dev")
@Slf4j
public class DataSeeder {

    // Consistent UUIDs for cross-service references (matching Auth Service seed data)
    private static final String CUSTOMER_1_ID = "00000000-0000-0000-0000-000000000101";
    private static final String CUSTOMER_2_ID = "00000000-0000-0000-0000-000000000102";
    private static final String EMPLOYEE_1_ID = "00000000-0000-0000-0000-000000000003";

    // Service/Project IDs (should match Service Management seed data)
    private static final String SERVICE_1_ID = "SRV-001";
    private static final String SERVICE_2_ID = "SRV-002";
    private static final String SERVICE_3_ID = "SRV-003";
    private static final String PROJECT_1_ID = "PRJ-001";

    @Bean
    CommandLineRunner initDatabase(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            PaymentRepository paymentRepository,
            ScheduledPaymentRepository scheduledPaymentRepository) {
        
        return args -> {
            if (invoiceRepository.count() > 0) {
                log.info("Database already seeded. Skipping data seeding.");
                return;
            }

            log.info("Starting Payment Service data seeding...");

            // Create invoices for completed services
            List<Invoice> invoices = createInvoices(invoiceRepository, invoiceItemRepository);
            
            // Create payments for invoices
            createPayments(paymentRepository, invoices);
            
            // Create scheduled payments
            createScheduledPayments(scheduledPaymentRepository, invoices);

            log.info("Payment Service data seeding completed successfully!");
            log.info("Created {} invoices", invoices.size());
            log.info("Sample customer IDs: {}, {}", CUSTOMER_1_ID, CUSTOMER_2_ID);
        };
    }

    private List<Invoice> createInvoices(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository) {
        
        List<Invoice> invoices = new ArrayList<>();

        // Invoice 1: Oil Change Service - PAID
        Invoice invoice1 = Invoice.builder()
                .customerId(CUSTOMER_1_ID)
                .serviceOrProjectId(SERVICE_1_ID)
                .amount(new BigDecimal("7000.00"))
                .status(InvoiceStatus.PAID)
                .issueDate(LocalDate.now().minusDays(30))
                .dueDate(LocalDate.now().minusDays(0))
                .notes("Auto-generated invoice for completed service")
                .build();
        
        InvoiceItem item1_1 = InvoiceItem.builder()
                .invoice(invoice1)
                .description("Oil Change - Synthetic Oil")
                .quantity(1)
                .unitPrice(new BigDecimal("5000.00"))
                .totalPrice(new BigDecimal("5000.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item1_2 = InvoiceItem.builder()
                .invoice(invoice1)
                .description("Oil Filter Replacement")
                .quantity(1)
                .unitPrice(new BigDecimal("800.00"))
                .totalPrice(new BigDecimal("800.00"))
                .itemType("PARTS")
                .build();
        InvoiceItem item1_3 = InvoiceItem.builder()
                .invoice(invoice1)
                .description("Labor - Oil Change")
                .quantity(1)
                .unitPrice(new BigDecimal("1200.00"))
                .totalPrice(new BigDecimal("1200.00"))
                .itemType("LABOR")
                .build();
        invoice1.setItems(new ArrayList<>(List.of(item1_1, item1_2, item1_3)));
        invoices.add(invoiceRepository.save(invoice1));

        // Invoice 2: Brake Service - PARTIALLY_PAID
        Invoice invoice2 = Invoice.builder()
                .customerId(CUSTOMER_1_ID)
                .serviceOrProjectId(SERVICE_2_ID)
                .amount(new BigDecimal("22000.00"))
                .status(InvoiceStatus.PARTIALLY_PAID)
                .issueDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().plusDays(10))
                .notes("Auto-generated invoice for completed service")
                .build();
        
        InvoiceItem item2_1 = InvoiceItem.builder()
                .invoice(invoice2)
                .description("Brake Pad Replacement - Front")
                .quantity(1)
                .unitPrice(new BigDecimal("8500.00"))
                .totalPrice(new BigDecimal("8500.00"))
                .itemType("PARTS")
                .build();
        InvoiceItem item2_2 = InvoiceItem.builder()
                .invoice(invoice2)
                .description("Brake Rotor Resurfacing")
                .quantity(2)
                .unitPrice(new BigDecimal("3000.00"))
                .totalPrice(new BigDecimal("6000.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item2_3 = InvoiceItem.builder()
                .invoice(invoice2)
                .description("Brake Fluid Flush")
                .quantity(1)
                .unitPrice(new BigDecimal("2500.00"))
                .totalPrice(new BigDecimal("2500.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item2_4 = InvoiceItem.builder()
                .invoice(invoice2)
                .description("Labor - Brake Service")
                .quantity(1)
                .unitPrice(new BigDecimal("5000.00"))
                .totalPrice(new BigDecimal("5000.00"))
                .itemType("LABOR")
                .build();
        invoice2.setItems(new ArrayList<>(List.of(item2_1, item2_2, item2_3, item2_4)));
        invoices.add(invoiceRepository.save(invoice2));

        // Invoice 3: Tire Rotation & Alignment - SENT (unpaid)
        Invoice invoice3 = Invoice.builder()
                .customerId(CUSTOMER_2_ID)
                .serviceOrProjectId(SERVICE_3_ID)
                .amount(new BigDecimal("12000.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(20))
                .notes("Auto-generated invoice for completed service")
                .build();
        
        InvoiceItem item3_1 = InvoiceItem.builder()
                .invoice(invoice3)
                .description("Tire Rotation - All Four Wheels")
                .quantity(1)
                .unitPrice(new BigDecimal("3500.00"))
                .totalPrice(new BigDecimal("3500.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item3_2 = InvoiceItem.builder()
                .invoice(invoice3)
                .description("Wheel Alignment - 4-Wheel")
                .quantity(1)
                .unitPrice(new BigDecimal("6000.00"))
                .totalPrice(new BigDecimal("6000.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item3_3 = InvoiceItem.builder()
                .invoice(invoice3)
                .description("Tire Pressure Check")
                .quantity(1)
                .unitPrice(new BigDecimal("500.00"))
                .totalPrice(new BigDecimal("500.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item3_4 = InvoiceItem.builder()
                .invoice(invoice3)
                .description("Labor - Tire Service")
                .quantity(1)
                .unitPrice(new BigDecimal("2000.00"))
                .totalPrice(new BigDecimal("2000.00"))
                .itemType("LABOR")
                .build();
        invoice3.setItems(new ArrayList<>(List.of(item3_1, item3_2, item3_3, item3_4)));
        invoices.add(invoiceRepository.save(invoice3));

        // Invoice 4: Custom Modification Project - SENT
        Invoice invoice4 = Invoice.builder()
                .customerId(CUSTOMER_2_ID)
                .serviceOrProjectId(PROJECT_1_ID)
                .amount(new BigDecimal("160000.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(25))
                .notes("Auto-generated invoice for completed service")
                .build();
        
        InvoiceItem item4_1 = InvoiceItem.builder()
                .invoice(invoice4)
                .description("Custom Exhaust System Installation")
                .quantity(1)
                .unitPrice(new BigDecimal("45000.00"))
                .totalPrice(new BigDecimal("45000.00"))
                .itemType("PARTS")
                .build();
        InvoiceItem item4_2 = InvoiceItem.builder()
                .invoice(invoice4)
                .description("Performance Air Intake")
                .quantity(1)
                .unitPrice(new BigDecimal("25000.00"))
                .totalPrice(new BigDecimal("25000.00"))
                .itemType("PARTS")
                .build();
        InvoiceItem item4_3 = InvoiceItem.builder()
                .invoice(invoice4)
                .description("ECU Tuning")
                .quantity(1)
                .unitPrice(new BigDecimal("35000.00"))
                .totalPrice(new BigDecimal("35000.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item4_4 = InvoiceItem.builder()
                .invoice(invoice4)
                .description("Labor - Custom Modifications")
                .quantity(1)
                .unitPrice(new BigDecimal("50000.00"))
                .totalPrice(new BigDecimal("50000.00"))
                .itemType("LABOR")
                .build();
        InvoiceItem item4_5 = InvoiceItem.builder()
                .invoice(invoice4)
                .description("Parts Sourcing Fee")
                .quantity(1)
                .unitPrice(new BigDecimal("5000.00"))
                .totalPrice(new BigDecimal("5000.00"))
                .itemType("SERVICE_FEE")
                .build();
        invoice4.setItems(new ArrayList<>(List.of(item4_1, item4_2, item4_3, item4_4, item4_5)));
        invoices.add(invoiceRepository.save(invoice4));

        // Invoice 5: Engine Diagnostic - OVERDUE
        Invoice invoice5 = Invoice.builder()
                .customerId(CUSTOMER_1_ID)
                .serviceOrProjectId("SRV-004")
                .amount(new BigDecimal("13800.00"))
                .status(InvoiceStatus.OVERDUE)
                .issueDate(LocalDate.now().minusDays(45))
                .dueDate(LocalDate.now().minusDays(15))
                .notes("Auto-generated invoice for completed service")
                .build();
        
        InvoiceItem item5_1 = InvoiceItem.builder()
                .invoice(invoice5)
                .description("Engine Diagnostic Scan")
                .quantity(1)
                .unitPrice(new BigDecimal("4000.00"))
                .totalPrice(new BigDecimal("4000.00"))
                .itemType("SERVICE")
                .build();
        InvoiceItem item5_2 = InvoiceItem.builder()
                .invoice(invoice5)
                .description("Spark Plug Replacement")
                .quantity(4)
                .unitPrice(new BigDecimal("1200.00"))
                .totalPrice(new BigDecimal("4800.00"))
                .itemType("PARTS")
                .build();
        InvoiceItem item5_3 = InvoiceItem.builder()
                .invoice(invoice5)
                .description("Air Filter Replacement")
                .quantity(1)
                .unitPrice(new BigDecimal("1500.00"))
                .totalPrice(new BigDecimal("1500.00"))
                .itemType("PARTS")
                .build();
        InvoiceItem item5_4 = InvoiceItem.builder()
                .invoice(invoice5)
                .description("Labor - Engine Service")
                .quantity(1)
                .unitPrice(new BigDecimal("3500.00"))
                .totalPrice(new BigDecimal("3500.00"))
                .itemType("LABOR")
                .build();
        invoice5.setItems(new ArrayList<>(List.of(item5_1, item5_2, item5_3, item5_4)));
        invoices.add(invoiceRepository.save(invoice5));

        log.info("Created {} invoices with line items", invoices.size());
        return invoices;
    }

    private void createPayments(PaymentRepository paymentRepository, List<Invoice> invoices) {
        List<Payment> payments = new ArrayList<>();

        // Payment 1: Full payment for first invoice
        Payment payment1 = Payment.builder()
                .invoiceId(invoices.get(0).getId())
                .customerId(CUSTOMER_1_ID)
                .amount(new BigDecimal("7000.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .paymentGatewayTransactionId("PH-TXN-001")
                .notes("Paid via PayHere")
                .build();
        payments.add(payment1);

        // Payment 2: Partial payment for second invoice
        Payment payment2 = Payment.builder()
                .invoiceId(invoices.get(1).getId())
                .customerId(CUSTOMER_1_ID)
                .amount(new BigDecimal("10000.00"))
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.SUCCESS)
                .notes("Partial payment - Bank transfer")
                .build();
        payments.add(payment2);

        // Payment 3: Pending payment for third invoice
        Payment payment3 = Payment.builder()
                .invoiceId(invoices.get(2).getId())
                .customerId(CUSTOMER_2_ID)
                .amount(new BigDecimal("12000.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.PENDING)
                .notes("Payment initiated via PayHere")
                .build();
        payments.add(payment3);

        // Payment 4: Failed payment attempt for fifth invoice
        Payment payment4 = Payment.builder()
                .invoiceId(invoices.get(4).getId())
                .customerId(CUSTOMER_1_ID)
                .amount(new BigDecimal("13800.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.FAILED)
                .notes("Payment failed - Insufficient funds")
                .build();
        payments.add(payment4);

        paymentRepository.saveAll(payments);
        log.info("Created {} payment records", payments.size());
    }

    private void createScheduledPayments(
            ScheduledPaymentRepository repository,
            List<Invoice> invoices) {
        
        List<ScheduledPayment> scheduledPayments = new ArrayList<>();

        // Scheduled payment 1: Remaining balance for second invoice
        ScheduledPayment scheduled1 = ScheduledPayment.builder()
                .invoiceId(invoices.get(1).getId())
                .customerId(CUSTOMER_1_ID)
                .amount(new BigDecimal("12000.00"))
                .scheduledDate(LocalDate.now().plusDays(10))
                .status("SCHEDULED")
                .notes("Scheduled payment for remaining brake service balance")
                .build();
        scheduledPayments.add(scheduled1);

        // Scheduled payment 2: Payment plan for fourth invoice (large project)
        ScheduledPayment scheduled2 = ScheduledPayment.builder()
                .invoiceId(invoices.get(3).getId())
                .customerId(CUSTOMER_2_ID)
                .amount(new BigDecimal("80000.00"))
                .scheduledDate(LocalDate.now().plusDays(15))
                .status("SCHEDULED")
                .notes("First installment - 50% of custom modification project")
                .build();
        scheduledPayments.add(scheduled2);

        // Scheduled payment 3: Second installment for fourth invoice
        ScheduledPayment scheduled3 = ScheduledPayment.builder()
                .invoiceId(invoices.get(3).getId())
                .customerId(CUSTOMER_2_ID)
                .amount(new BigDecimal("80000.00"))
                .scheduledDate(LocalDate.now().plusDays(45))
                .status("SCHEDULED")
                .notes("Final installment - 50% of custom modification project")
                .build();
        scheduledPayments.add(scheduled3);

        repository.saveAll(scheduledPayments);
        log.info("Created {} scheduled payment records", scheduledPayments.size());
    }
}
