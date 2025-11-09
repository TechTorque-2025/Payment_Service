package com.techtorque.payment_service.service.impl;

import com.techtorque.payment_service.config.PayHereConfig;
import com.techtorque.payment_service.dto.PaymentInitiationDto;
import com.techtorque.payment_service.dto.PaymentInitiationResponseDto;
import com.techtorque.payment_service.dto.request.CreateInvoiceDto;
import com.techtorque.payment_service.dto.request.PaymentRequestDto;
import com.techtorque.payment_service.dto.request.SchedulePaymentDto;
import com.techtorque.payment_service.dto.response.InvoiceItemDto;
import com.techtorque.payment_service.dto.response.InvoiceResponseDto;
import com.techtorque.payment_service.dto.response.PaymentResponseDto;
import com.techtorque.payment_service.dto.response.ScheduledPaymentResponseDto;
import com.techtorque.payment_service.entity.*;
import com.techtorque.payment_service.exception.InvoiceNotFoundException;
import com.techtorque.payment_service.exception.InvalidPaymentException;
import com.techtorque.payment_service.exception.PaymentNotFoundException;
import com.techtorque.payment_service.exception.UnauthorizedAccessException;
import com.techtorque.payment_service.repository.InvoiceItemRepository;
import com.techtorque.payment_service.repository.InvoiceRepository;
import com.techtorque.payment_service.repository.PaymentRepository;
import com.techtorque.payment_service.repository.ScheduledPaymentRepository;
import com.techtorque.payment_service.service.BillingService;
import com.techtorque.payment_service.util.PayHereHashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class BillingServiceImpl implements BillingService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceItemRepository invoiceItemRepository;
  private final PaymentRepository paymentRepository;
  private final ScheduledPaymentRepository scheduledPaymentRepository;
  private final WebClient.Builder webClientBuilder;
  private final PayHereConfig payHereConfig;

  public BillingServiceImpl(
      InvoiceRepository invoiceRepository,
      InvoiceItemRepository invoiceItemRepository,
      PaymentRepository paymentRepository,
      ScheduledPaymentRepository scheduledPaymentRepository,
      WebClient.Builder webClientBuilder,
      PayHereConfig payHereConfig) {
    this.invoiceRepository = invoiceRepository;
    this.invoiceItemRepository = invoiceItemRepository;
    this.paymentRepository = paymentRepository;
    this.scheduledPaymentRepository = scheduledPaymentRepository;
    this.webClientBuilder = webClientBuilder;
    this.payHereConfig = payHereConfig;
  }

  @Override
  public InvoiceResponseDto createInvoice(CreateInvoiceDto dto) {
    log.info("Creating invoice for customer: {}", dto.getCustomerId());
    
    // Create invoice entity
    Invoice invoice = Invoice.builder()
        .customerId(dto.getCustomerId())
        .serviceOrProjectId(dto.getServiceOrProjectId())
        .status(InvoiceStatus.DRAFT)
        .dueDate(dto.getDueDate() != null ? dto.getDueDate() : LocalDate.now().plusDays(30))
        .notes(dto.getNotes())
        .build();
    
    // Calculate total from items
    BigDecimal total = BigDecimal.ZERO;
    for (CreateInvoiceDto.InvoiceItemRequest itemDto : dto.getItems()) {
      BigDecimal itemTotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
      total = total.add(itemTotal);
    }
    invoice.setAmount(total);
    
    // Save invoice first
    invoice = invoiceRepository.save(invoice);
    
    // Create and save invoice items
    final Invoice savedInvoice = invoice;
    for (CreateInvoiceDto.InvoiceItemRequest itemDto : dto.getItems()) {
      InvoiceItem item = InvoiceItem.builder()
          .invoice(savedInvoice)
          .description(itemDto.getDescription())
          .quantity(itemDto.getQuantity())
          .unitPrice(itemDto.getUnitPrice())
          .itemType(itemDto.getItemType())
          .build();
      invoiceItemRepository.save(item);
    }
    
    log.info("Invoice created successfully: {}", invoice.getId());
    return mapToInvoiceResponseDto(invoice);
  }

  @Override
  public InvoiceResponseDto createInvoiceForService(String serviceId, String customerId, CreateInvoiceDto dto) {
    log.info("Creating invoice for service: {} and customer: {}", serviceId, customerId);
    
    // Ensure the service/project ID matches
    CreateInvoiceDto invoiceDto = new CreateInvoiceDto();
    invoiceDto.setCustomerId(customerId);
    invoiceDto.setServiceOrProjectId(serviceId);
    invoiceDto.setItems(dto.getItems());
    invoiceDto.setDueDate(dto.getDueDate());
    invoiceDto.setNotes(dto.getNotes());
    
    return createInvoice(invoiceDto);
  }

  @Override
  @Transactional(readOnly = true)
  public InvoiceResponseDto getInvoiceById(String invoiceId, String userId) {
    log.info("Fetching invoice: {} for user: {}", invoiceId, userId);
    
    Invoice invoice = invoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));
    
    // Verify user has access to this invoice
    if (!invoice.getCustomerId().equals(userId)) {
      throw new UnauthorizedAccessException("User does not have access to this invoice");
    }
    
    return mapToInvoiceResponseDto(invoice);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvoiceResponseDto> listInvoicesForCustomer(String customerId) {
    log.info("Listing invoices for customer: {}", customerId);
    
    List<Invoice> invoices = invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    return invoices.stream()
        .map(this::mapToInvoiceResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvoiceResponseDto> listAllInvoices() {
    log.info("Listing all invoices (admin/employee access)");
    
    List<Invoice> invoices = invoiceRepository.findAll();
    return invoices.stream()
        .map(this::mapToInvoiceResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  public void sendInvoice(String invoiceId, String email) {
    log.info("Sending invoice {} to email: {}", invoiceId, email);
    
    Invoice invoice = invoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));
    
    // Update invoice status to SENT
    if (invoice.getStatus() == InvoiceStatus.DRAFT) {
      invoice.setStatus(InvoiceStatus.SENT);
      invoiceRepository.save(invoice);
    }
    
    // TODO: Integrate with email service to send invoice
    // For now, just log it
    log.info("Invoice {} would be sent to {}. Email integration pending.", invoiceId, email);
  }

  @Override
  public PaymentResponseDto processPayment(PaymentRequestDto dto, String customerId) {
    log.info("Processing payment for invoice: {} by customer: {}", dto.getInvoiceId(), customerId);
    
    // Validate invoice exists and belongs to customer
    Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
        .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + dto.getInvoiceId()));
    
    if (!invoice.getCustomerId().equals(customerId)) {
      throw new UnauthorizedAccessException("Customer does not have access to this invoice");
    }
    
    // Validate payment amount
    if (dto.getAmount().compareTo(invoice.getAmount()) > 0) {
      throw new InvalidPaymentException("Payment amount exceeds invoice amount");
    }
    
    if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidPaymentException("Payment amount must be positive");
    }
    
    // Create payment record
    Payment payment = Payment.builder()
        .invoiceId(dto.getInvoiceId())
        .customerId(customerId)
        .amount(dto.getAmount())
        .method(dto.getMethod())
        .status(PaymentStatus.PENDING)
        .build();
    
    // For CASH or BANK_TRANSFER, immediately mark as SUCCESS
    // For CARD, would normally integrate with payment gateway
    if (dto.getMethod() == PaymentMethod.CASH || dto.getMethod() == PaymentMethod.BANK_TRANSFER) {
      payment.setStatus(PaymentStatus.SUCCESS);
      
      // Update invoice status
      updateInvoiceStatus(invoice, dto.getAmount());
    }
    
    payment = paymentRepository.save(payment);
    
    log.info("Payment processed successfully: {}", payment.getId());
    return mapToPaymentResponseDto(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponseDto> getPaymentHistoryForCustomer(String customerId) {
    log.info("Fetching payment history for customer: {}", customerId);
    
    List<Payment> payments = paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    return payments.stream()
        .map(this::mapToPaymentResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentResponseDto getPaymentDetails(String paymentId, String userId) {
    log.info("Fetching payment details: {} for user: {}", paymentId, userId);
    
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
    
    // Verify user has access to this payment
    if (!payment.getCustomerId().equals(userId)) {
      throw new UnauthorizedAccessException("User does not have access to this payment");
    }
    
    return mapToPaymentResponseDto(payment);
  }

  @Override
  public ScheduledPaymentResponseDto schedulePayment(SchedulePaymentDto dto, String customerId) {
    log.info("Scheduling payment for invoice: {} by customer: {}", dto.getInvoiceId(), customerId);
    
    // Validate invoice exists and belongs to customer
    Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
        .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + dto.getInvoiceId()));
    
    if (!invoice.getCustomerId().equals(customerId)) {
      throw new UnauthorizedAccessException("Customer does not have access to this invoice");
    }
    
    // Validate amount
    if (dto.getAmount().compareTo(invoice.getAmount()) > 0) {
      throw new InvalidPaymentException("Scheduled payment amount exceeds invoice amount");
    }
    
    // Create scheduled payment
    ScheduledPayment scheduledPayment = ScheduledPayment.builder()
        .invoiceId(dto.getInvoiceId())
        .customerId(customerId)
        .amount(dto.getAmount())
        .scheduledDate(dto.getScheduledDate())
        .status("SCHEDULED")
        .notes(dto.getNotes())
        .build();
    
    scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
    
    log.info("Payment scheduled successfully: {}", scheduledPayment.getId());
    return mapToScheduledPaymentResponseDto(scheduledPayment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ScheduledPaymentResponseDto> getScheduledPaymentsForCustomer(String customerId) {
    log.info("Fetching scheduled payments for customer: {}", customerId);
    
    List<ScheduledPayment> scheduledPayments = scheduledPaymentRepository.findByCustomerId(customerId);
    return scheduledPayments.stream()
        .map(this::mapToScheduledPaymentResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  public PaymentInitiationResponseDto initiatePayHerePayment(PaymentInitiationDto dto) {
    log.info("Initiating PayHere payment for invoice: {}", dto.getInvoiceId());

    // Format amount to 2 decimal places as per PayHere requirements
    String formattedAmount = String.format("%.2f", dto.getAmount());

    // Generate hash according to PayHere documentation:
    // Hash = MD5(merchant_id + order_id + amount + currency + MD5(merchant_secret).toUpperCase()).toUpperCase()
    log.info("DEBUG - Merchant ID: {}", payHereConfig.getMerchantId());
    log.info("DEBUG - Order ID: {}", dto.getInvoiceId());
    log.info("DEBUG - Formatted Amount: {}", formattedAmount);
    log.info("DEBUG - Currency: {}", dto.getCurrency());
    log.info("DEBUG - Merchant Secret (first 10 chars): {}", payHereConfig.getMerchantSecret().substring(0, Math.min(10, payHereConfig.getMerchantSecret().length())));

    // Use merchant secret as-is (without base64 decoding)
    String hashedSecret = PayHereHashUtil.getMd5(payHereConfig.getMerchantSecret());
    log.info("DEBUG - Hashed Secret: {}", hashedSecret);

    String hashString = payHereConfig.getMerchantId() + dto.getInvoiceId() +
                       formattedAmount + dto.getCurrency() + hashedSecret;
    log.info("DEBUG - Hash String: {}", hashString);

    String hash = PayHereHashUtil.getMd5(hashString);
    log.info("DEBUG - Final Hash: {}", hash);

    // Build response with all PayHere required parameters
    PaymentInitiationResponseDto response = new PaymentInitiationResponseDto();
    response.setMerchantId(payHereConfig.getMerchantId());
    response.setOrderId(dto.getInvoiceId());  // Using invoiceId as orderId for PayHere
    response.setAmount(new BigDecimal(formattedAmount));
    response.setCurrency(dto.getCurrency());
    response.setHash(hash);
    response.setItemDescription(dto.getItemDescription());
    response.setReturnUrl(payHereConfig.getReturnUrl());
    response.setCancelUrl(payHereConfig.getCancelUrl());
    response.setNotifyUrl(payHereConfig.getNotifyUrl());
    response.setCustomerFirstName(dto.getCustomerFirstName());
    response.setCustomerLastName(dto.getCustomerLastName());
    response.setCustomerEmail(dto.getCustomerEmail());
    response.setCustomerPhone(dto.getCustomerPhone());
    response.setCustomerAddress(dto.getCustomerAddress());
    response.setCustomerCity(dto.getCustomerCity());
    response.setSandbox(payHereConfig.isSandbox());

    // Create Payment entity with PENDING status
    Payment payment = Payment.builder()
        .invoiceId(dto.getInvoiceId())
        .customerId(dto.getCustomerEmail()) // Using email as temporary customer ID
        .amount(new BigDecimal(formattedAmount))
        .method(PaymentMethod.CARD)
        .status(PaymentStatus.PENDING)
        .build();

    paymentRepository.save(payment);

    log.info("PayHere payment initiated for invoice: {}", dto.getInvoiceId());
    return response;
  }

  @Override
  public void verifyAndProcessNotification(MultiValueMap<String, String> formData) {
    log.info("Processing PayHere notification");
    
    // Extract PayHere notification data
    String merchantId = formData.getFirst("merchant_id");
    String orderId = formData.getFirst("order_id");
    String payhereAmount = formData.getFirst("payhere_amount");
    String payhereCurrency = formData.getFirst("payhere_currency");
    String statusCode = formData.getFirst("status_code");
    String md5sig = formData.getFirst("md5sig");
    String paymentId = formData.getFirst("payment_id");

    // Generate hash for verification
    String hashString = merchantId + orderId + payhereAmount + payhereCurrency + 
                       statusCode + payHereConfig.getMerchantSecret();
    String generatedHash = PayHereHashUtil.getMd5(hashString);

    // Verify signature
    if (!generatedHash.equalsIgnoreCase(md5sig)) {
      log.error("Invalid PayHere signature for order: {}", orderId);
      throw new InvalidPaymentException("Invalid PayHere signature");
    }

    log.info("PayHere signature verified for order: {}", orderId);

    // Find payment by invoice ID (orderId)
    List<Payment> payments = paymentRepository.findByInvoiceId(orderId);
    
    if (payments.isEmpty()) {
      log.warn("No payment found for order: {}", orderId);
      return;
    }
    
    Payment payment = payments.get(0); // Get the most recent payment

    // Process payment based on status_code
    // 2 = success, 0 = pending, -1 = canceled, -2 = failed, -3 = chargedback
    if ("2".equals(statusCode)) {
      payment.setStatus(PaymentStatus.SUCCESS);
      payment.setPaymentGatewayTransactionId(paymentId);
      paymentRepository.save(payment);
      
      // Update invoice status
      Invoice invoice = invoiceRepository.findById(orderId).orElse(null);
      if (invoice != null) {
        updateInvoiceStatus(invoice, payment.getAmount());
      }
      
      log.info("Payment successful for order: {}", orderId);
    } else {
      payment.setStatus(PaymentStatus.FAILED);
      payment.setNotes("PayHere status code: " + statusCode);
      paymentRepository.save(payment);
      
      log.info("Payment failed for order: {} with status: {}", orderId, statusCode);
    }
  }

  // Helper methods

  private void updateInvoiceStatus(Invoice invoice, BigDecimal paymentAmount) {
    // Calculate total paid
    List<Payment> successfulPayments = paymentRepository.findByInvoiceId(invoice.getId())
        .stream()
        .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
        .collect(Collectors.toList());
    
    BigDecimal totalPaid = successfulPayments.stream()
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // Update invoice status based on payment
    if (totalPaid.compareTo(invoice.getAmount()) >= 0) {
      invoice.setStatus(InvoiceStatus.PAID);
    } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
      invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
    }
    
    invoiceRepository.save(invoice);
  }

  private InvoiceResponseDto mapToInvoiceResponseDto(Invoice invoice) {
    // Fetch invoice items
    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());

    // Calculate total paid
    List<Payment> successfulPayments = paymentRepository.findByInvoiceId(invoice.getId())
        .stream()
        .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
        .collect(Collectors.toList());

    BigDecimal paidAmount = successfulPayments.stream()
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal balanceAmount = invoice.getAmount().subtract(paidAmount);

    // Calculate subtotal (sum of all items), tax and discount are 0 for now
    BigDecimal subtotal = items.stream()
        .map(InvoiceItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Find the latest successful payment for paidAt timestamp
    java.time.LocalDateTime paidAt = successfulPayments.stream()
        .map(Payment::getCreatedAt)
        .max((a, b) -> a.compareTo(b))
        .orElse(null);

    // Generate invoice number from ID (INV-{first 8 chars of UUID})
    String invoiceNumber = "INV-" + invoice.getId().substring(0, 8).toUpperCase();

    return InvoiceResponseDto.builder()
        .invoiceId(invoice.getId())
        .invoiceNumber(invoiceNumber)
        .customerId(invoice.getCustomerId())
        .customerName(null) // TODO: Fetch from user service
        .customerEmail(null) // TODO: Fetch from user service
        .serviceId(invoice.getServiceOrProjectId().startsWith("SRV") ? invoice.getServiceOrProjectId() : null)
        .projectId(invoice.getServiceOrProjectId().startsWith("PRJ") ? invoice.getServiceOrProjectId() : null)
        .items(items.stream().map(this::mapToInvoiceItemDto).collect(Collectors.toList()))
        .subtotal(subtotal)
        .taxAmount(BigDecimal.ZERO) // TODO: Calculate tax if needed
        .discountAmount(BigDecimal.ZERO) // TODO: Calculate discount if needed
        .totalAmount(invoice.getAmount())
        .paidAmount(paidAmount)
        .balanceAmount(balanceAmount)
        .status(invoice.getStatus())
        .notes(invoice.getNotes())
        .dueDate(invoice.getDueDate())
        .issuedAt(invoice.getIssueDate() != null ? invoice.getIssueDate().atStartOfDay() : null)
        .paidAt(paidAt)
        .createdAt(invoice.getCreatedAt())
        .updatedAt(invoice.getUpdatedAt())
        .build();
  }

  private InvoiceItemDto mapToInvoiceItemDto(InvoiceItem item) {
    return InvoiceItemDto.builder()
        .itemId(item.getId())
        .description(item.getDescription())
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice())
        .totalPrice(item.getTotalPrice())
        .itemType(item.getItemType())
        .build();
  }

  private PaymentResponseDto mapToPaymentResponseDto(Payment payment) {
    return PaymentResponseDto.builder()
        .paymentId(payment.getId())
        .invoiceId(payment.getInvoiceId())
        .amount(payment.getAmount())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .paymentGatewayTransactionId(payment.getPaymentGatewayTransactionId())
        .createdAt(payment.getCreatedAt())
        .build();
  }

  private ScheduledPaymentResponseDto mapToScheduledPaymentResponseDto(ScheduledPayment scheduledPayment) {
    return ScheduledPaymentResponseDto.builder()
        .scheduleId(scheduledPayment.getId())
        .invoiceId(scheduledPayment.getInvoiceId())
        .customerId(scheduledPayment.getCustomerId())
        .amount(scheduledPayment.getAmount())
        .scheduledDate(scheduledPayment.getScheduledDate())
        .status(scheduledPayment.getStatus())
        .notes(scheduledPayment.getNotes())
        .createdAt(scheduledPayment.getCreatedAt())
        .build();
  }
}