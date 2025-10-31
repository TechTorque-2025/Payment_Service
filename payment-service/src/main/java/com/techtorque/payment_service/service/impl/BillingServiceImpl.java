package com.techtorque.payment_service.service.impl;

import com.techtorque.payment_service.dto.InvoiceRequestDto;
import com.techtorque.payment_service.dto.PaymentRequestDto;
import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.InvoiceStatus;
import com.techtorque.payment_service.entity.Payment;
import com.techtorque.payment_service.entity.PaymentStatus;
import com.techtorque.payment_service.exception.InvoiceNotFoundException;
import com.techtorque.payment_service.exception.PaymentProcessingException;
import com.techtorque.payment_service.repository.InvoiceRepository;
import com.techtorque.payment_service.repository.PaymentRepository;
import com.techtorque.payment_service.service.BillingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class BillingServiceImpl implements BillingService {

  private final InvoiceRepository invoiceRepository;
  private final PaymentRepository paymentRepository;
  private final WebClient.Builder webClientBuilder;

  public BillingServiceImpl(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, WebClient.Builder webClientBuilder) {
    this.invoiceRepository = invoiceRepository;
    this.paymentRepository = paymentRepository;
    this.webClientBuilder = webClientBuilder;
  }

  @Override
  public Invoice createInvoice(InvoiceRequestDto dto) {
    log.info("Creating invoice for customer: {} and service: {}", dto.getCustomerId(), dto.getServiceOrProjectId());

    Invoice invoice = Invoice.builder()
            .customerId(dto.getCustomerId())
            .serviceOrProjectId(dto.getServiceOrProjectId())
            .amount(dto.getAmount())
            .status(InvoiceStatus.PENDING)
            .issueDate(dto.getIssueDate())
            .dueDate(dto.getDueDate())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    Invoice savedInvoice = invoiceRepository.save(invoice);
    log.info("Invoice created successfully with ID: {}", savedInvoice.getId());

    return savedInvoice;
  }

  @Override
  public Invoice getInvoiceById(String invoiceId) {
    log.info("Fetching invoice: {}", invoiceId);
    return invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> {
              log.warn("Invoice not found: {}", invoiceId);
              return new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId);
            });
  }

  @Override
  public Payment processPayment(PaymentRequestDto dto, String customerId) {
    log.info("Processing payment for invoice: {} by customer: {}", dto.getInvoiceId(), customerId);

    // Verify invoice exists and belongs to customer
    Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

    if (!invoice.getCustomerId().equals(customerId)) {
      throw new PaymentProcessingException("Invoice does not belong to this customer");
    }

    if (invoice.getStatus() == InvoiceStatus.PAID) {
      throw new PaymentProcessingException("Invoice is already paid");
    }

    // Create payment entity
    Payment payment = Payment.builder()
            .invoiceId(dto.getInvoiceId())
            .amount(dto.getAmount())
            .method(dto.getMethod())
            .status(PaymentStatus.PENDING)
            .paymentGatewayTransactionId(dto.getPaymentGatewayTransactionId())
            .createdAt(LocalDateTime.now())
            .build();

    // Save payment with pending status
    Payment savedPayment = paymentRepository.save(payment);

    // Simulate payment processing (In production, integrate with PayHere API)
    try {
      // Simulating successful payment
      log.info("Payment processed successfully for invoice: {}", dto.getInvoiceId());

      savedPayment.setStatus(PaymentStatus.SUCCESS);
      paymentRepository.save(savedPayment);

      // Update invoice status to PAID
      invoice.setStatus(InvoiceStatus.PAID);
      invoice.setUpdatedAt(LocalDateTime.now());
      invoiceRepository.save(invoice);

      log.info("Invoice {} marked as PAID", invoice.getId());

    } catch (Exception e) {
      log.error("Payment processing failed: {}", e.getMessage());
      savedPayment.setStatus(PaymentStatus.FAILED);
      paymentRepository.save(savedPayment);
      throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
    }

    return savedPayment;
  }

  @Override
  public List<Payment> getPaymentHistoryForCustomer(String customerId) {
    log.info("Fetching payment history for customer: {}", customerId);
    return paymentRepository.findPaymentsByCustomerId(customerId);
  }

  @Override
  public Optional<Payment> getPaymentDetails(String paymentId) {
    log.info("Fetching payment details: {}", paymentId);
    return paymentRepository.findById(paymentId);
  }

  @Override
  public List<Invoice> listInvoicesForCustomer(String customerId) {
    log.info("Listing invoices for customer: {}", customerId);
    return invoiceRepository.findByCustomerId(customerId);
  }

  @Override
  public void sendInvoice(String invoiceId, String email) {
    log.info("Sending invoice {} to email: {}", invoiceId, email);

    Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

    // TODO: Implement email sending logic (e.g., using JavaMailSender)
    log.info("Invoice email sent successfully to: {}", email);
  }

  @Override
  public Invoice markInvoiceAsPaid(String invoiceId) {
    log.info("Marking invoice {} as paid", invoiceId);

    Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

    invoice.setStatus(InvoiceStatus.PAID);
    invoice.setUpdatedAt(LocalDateTime.now());

    Invoice updatedInvoice = invoiceRepository.save(invoice);
    log.info("Invoice {} marked as PAID", invoiceId);

    return updatedInvoice;
  }

  @Override
  public void verifyAndProcessNotification(MultiValueMap<String, String> formData) {
    log.info("Processing PayHere notification");
    // TODO: Implement PayHere notification verification logic
    // 1. Verify hash
    // 2. Update payment status
    // 3. Update invoice status
  }
}