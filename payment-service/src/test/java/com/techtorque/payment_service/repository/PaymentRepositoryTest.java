package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.Payment;
import com.techtorque.payment_service.entity.PaymentMethod;
import com.techtorque.payment_service.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        testPayment = Payment.builder()
                .invoiceId("invoice123")
                .customerId("customer123")
                .amount(new BigDecimal("500.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .paymentGatewayTransactionId("txn123")
                .notes("Test payment")
                .build();
    }

    @Test
    void testSavePayment() {
        Payment saved = paymentRepository.save(testPayment);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getInvoiceId()).isEqualTo("invoice123");
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void testFindById() {
        paymentRepository.save(testPayment);

        Optional<Payment> found = paymentRepository.findById(testPayment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getInvoiceId()).isEqualTo("invoice123");
    }

    @Test
    void testFindByInvoiceId() {
        Payment payment2 = Payment.builder()
                .invoiceId("invoice123")
                .customerId("customer456")
                .amount(new BigDecimal("300.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(testPayment);
        paymentRepository.save(payment2);

        List<Payment> payments = paymentRepository.findByInvoiceId("invoice123");

        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getInvoiceId().equals("invoice123"));
    }

    @Test
    void testFindByCustomerId() {
        Payment payment2 = Payment.builder()
                .invoiceId("invoice456")
                .customerId("customer123")
                .amount(new BigDecimal("200.00"))
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(testPayment);
        paymentRepository.save(payment2);

        List<Payment> payments = paymentRepository.findByCustomerId("customer123");

        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getCustomerId().equals("customer123"));
    }

    @Test
    void testFindByCustomerIdOrderByCreatedAtDesc() {
        Payment payment2 = Payment.builder()
                .invoiceId("invoice456")
                .customerId("customer123")
                .amount(new BigDecimal("200.00"))
                .method(PaymentMethod.ONLINE)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(testPayment);
        paymentRepository.save(payment2);

        List<Payment> payments = paymentRepository.findByCustomerIdOrderByCreatedAtDesc("customer123");

        assertThat(payments).hasSize(2);
        assertThat(payments.get(0).getCreatedAt()).isAfterOrEqualTo(payments.get(1).getCreatedAt());
    }

    @Test
    void testFindByStatus() {
        Payment payment2 = Payment.builder()
                .invoiceId("invoice456")
                .customerId("customer456")
                .amount(new BigDecimal("100.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .build();

        Payment payment3 = Payment.builder()
                .invoiceId("invoice789")
                .customerId("customer789")
                .amount(new BigDecimal("150.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(testPayment);
        paymentRepository.save(payment2);
        paymentRepository.save(payment3);

        List<Payment> successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

        assertThat(successfulPayments).hasSize(2);
        assertThat(successfulPayments).allMatch(p -> p.getStatus() == PaymentStatus.SUCCESS);
    }

    @Test
    void testFindByPaymentGatewayTransactionId() {
        paymentRepository.save(testPayment);

        Optional<Payment> found = paymentRepository.findByPaymentGatewayTransactionId("txn123");

        assertThat(found).isPresent();
        assertThat(found.get().getPaymentGatewayTransactionId()).isEqualTo("txn123");
    }

    @Test
    void testFindByCustomerIdAndStatus() {
        Payment payment2 = Payment.builder()
                .invoiceId("invoice456")
                .customerId("customer123")
                .amount(new BigDecimal("200.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .build();

        Payment payment3 = Payment.builder()
                .invoiceId("invoice789")
                .customerId("customer123")
                .amount(new BigDecimal("300.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(testPayment);
        paymentRepository.save(payment2);
        paymentRepository.save(payment3);

        List<Payment> successPayments = paymentRepository.findByCustomerIdAndStatus(
                "customer123", PaymentStatus.SUCCESS);

        assertThat(successPayments).hasSize(2);
        assertThat(successPayments).allMatch(p ->
                p.getCustomerId().equals("customer123") && p.getStatus() == PaymentStatus.SUCCESS);
    }

    @Test
    void testFindPaymentsBetweenDates() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(7);
        LocalDateTime endDate = now.plusDays(1);

        paymentRepository.save(testPayment);

        List<Payment> payments = paymentRepository.findPaymentsBetweenDates(startDate, endDate);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getCreatedAt()).isBetween(startDate, endDate);
    }

    @Test
    void testFindCustomerPaymentsBetweenDates() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(7);
        LocalDateTime endDate = now.plusDays(1);

        Payment payment2 = Payment.builder()
                .invoiceId("invoice456")
                .customerId("customer456")
                .amount(new BigDecimal("200.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(testPayment);
        paymentRepository.save(payment2);

        List<Payment> payments = paymentRepository.findCustomerPaymentsBetweenDates(
                "customer123", startDate, endDate);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getCustomerId()).isEqualTo("customer123");
        assertThat(payments.get(0).getCreatedAt()).isBetween(startDate, endDate);
    }

    @Test
    void testUpdatePayment() {
        paymentRepository.save(testPayment);

        testPayment.setStatus(PaymentStatus.FAILED);
        testPayment.setNotes("Payment failed due to insufficient funds");
        Payment updated = paymentRepository.save(testPayment);

        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(updated.getNotes()).contains("Payment failed");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void testDeletePayment() {
        paymentRepository.save(testPayment);
        String paymentId = testPayment.getId();

        paymentRepository.deleteById(paymentId);

        Optional<Payment> deleted = paymentRepository.findById(paymentId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testPaymentMethodTypes() {
        Payment cardPayment = Payment.builder()
                .invoiceId("inv1")
                .customerId("cust1")
                .amount(new BigDecimal("100.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .build();

        Payment cashPayment = Payment.builder()
                .invoiceId("inv2")
                .customerId("cust2")
                .amount(new BigDecimal("200.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.SUCCESS)
                .build();

        Payment bankTransferPayment = Payment.builder()
                .invoiceId("inv3")
                .customerId("cust3")
                .amount(new BigDecimal("300.00"))
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.SUCCESS)
                .build();

        Payment onlinePayment = Payment.builder()
                .invoiceId("inv4")
                .customerId("cust4")
                .amount(new BigDecimal("400.00"))
                .method(PaymentMethod.ONLINE)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(cardPayment);
        paymentRepository.save(cashPayment);
        paymentRepository.save(bankTransferPayment);
        paymentRepository.save(onlinePayment);

        List<Payment> allPayments = paymentRepository.findAll();

        assertThat(allPayments).hasSize(4);
        assertThat(allPayments).anyMatch(p -> p.getMethod() == PaymentMethod.CARD);
        assertThat(allPayments).anyMatch(p -> p.getMethod() == PaymentMethod.CASH);
        assertThat(allPayments).anyMatch(p -> p.getMethod() == PaymentMethod.BANK_TRANSFER);
        assertThat(allPayments).anyMatch(p -> p.getMethod() == PaymentMethod.ONLINE);
    }

    @Test
    void testDefaultStatusOnCreation() {
        Payment newPayment = Payment.builder()
                .invoiceId("invoice999")
                .customerId("customer999")
                .amount(new BigDecimal("100.00"))
                .method(PaymentMethod.CARD)
                .build();

        Payment saved = paymentRepository.save(newPayment);

        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}
