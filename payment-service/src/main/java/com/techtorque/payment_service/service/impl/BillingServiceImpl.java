package com.techtorque.payment_service.service.impl;

import com.techtorque.payment_service.config.PayHereConfig;
import com.techtorque.payment_service.dto.PaymentInitiationDto;
import com.techtorque.payment_service.dto.PaymentInitiationResponseDto;
import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.Payment;
import com.techtorque.payment_service.repository.InvoiceRepository;
import com.techtorque.payment_service.repository.PaymentRepository;
import com.techtorque.payment_service.service.BillingService;
import com.techtorque.payment_service.util.PayHereHashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient; // For external API calls

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

  private final InvoiceRepository invoiceRepository;
  private final PaymentRepository paymentRepository;
  private final WebClient.Builder webClientBuilder; // For calling PayHere
  private final PayHereConfig payHereConfig;

  public BillingServiceImpl(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, WebClient.Builder webClientBuilder, PayHereConfig payHereConfig) {
    this.invoiceRepository = invoiceRepository;
    this.paymentRepository = paymentRepository;
    this.webClientBuilder = webClientBuilder;
    this.payHereConfig = payHereConfig;
  }

  @Override
  public Invoice createInvoiceForService(String serviceId, String customerId /*, ... */) {
    // TODO: Logic to create an Invoice when a service is marked as complete.
    return null;
  }

  @Override
  public Payment processPayment(/* PaymentRequestDto dto, */ String customerId) {
    // TODO: Developer will implement this critical logic.
    // 1. Create a Payment entity with PENDING status and save it.
    // 2. Build a request to the PayHere API using WebClient.
    // 3. Make the external API call to process the payment.
    // 4. Based on the response from PayHere:
    //    - If successful, update the Payment status to SUCCESS, save the transaction ID.
    //    - Find the corresponding Invoice and update its status to PAID.
    //    - If failed, update the Payment status to FAILED.
    // 5. All these database updates must happen within this single @Transactional method.
    return null;
  }

  @Override
  public List<Payment> getPaymentHistoryForCustomer(String customerId) {
    return List.of();
  }

  @Override
  public Optional<Payment> getPaymentDetails(String paymentId, String userId) {
    return Optional.empty();
  }

  @Override
  public Object schedulePayment(String customerId) {
    return null;
  }

  @Override
  public List<Invoice> listInvoicesForCustomer(String customerId) {
    return List.of();
  }

  @Override
  public void sendInvoice(String invoiceId, String email) {

  }

  @Override
  public Object initiatePayment(String invoiceId) {
    return null;
  }

  @Override
  public void verifyAndProcessNotification(MultiValueMap<String, String> formData) {
    // Extract PayHere notification data
    String merchantId = formData.getFirst("merchant_id");
    String orderId = formData.getFirst("order_id");
    String payhereAmount = formData.getFirst("payhere_amount");
    String payhereCurrency = formData.getFirst("payhere_currency");
    String statusCode = formData.getFirst("status_code");
    String md5sig = formData.getFirst("md5sig");

    // Generate hash for verification: merchant_id + order_id + payhere_amount + payhere_currency + status_code + merchant_secret
    String hashString = merchantId + orderId + payhereAmount + payhereCurrency + statusCode + payHereConfig.getMerchantSecret();
    String generatedHash = PayHereHashUtil.getMd5(hashString);

    // Verify signature
    if (!generatedHash.equals(md5sig)) {
      throw new RuntimeException("Invalid PayHere signature");
    }

    // Process payment based on status_code
    // 2 = success, 0 = pending, -1 = canceled, -2 = failed, -3 = chargedback
    if ("2".equals(statusCode)) {
      // TODO: Update payment status to SUCCESS and invoice status to PAID
      // Find Payment by orderId and update status
      System.out.println("Payment successful for order: " + orderId);
    } else {
      // TODO: Update payment status to FAILED
      System.out.println("Payment failed/cancelled for order: " + orderId + " with status: " + statusCode);
    }
  }

  @Override
  public PaymentInitiationResponseDto initiatePayHerePayment(PaymentInitiationDto dto) {
    // Format amount to 2 decimal places as per PayHere requirements
    String formattedAmount = String.format("%.2f", dto.getAmount());

    // Generate hash according to PayHere documentation:
    // Hash = MD5(merchant_id + order_id + amount + currency + MD5(merchant_secret).toUpperCase()).toUpperCase()
    String hashedSecret = PayHereHashUtil.getMd5(payHereConfig.getMerchantSecret());
    String hashString = payHereConfig.getMerchantId() + dto.getOrderId() +
                       formattedAmount + dto.getCurrency() + hashedSecret;
    String hash = PayHereHashUtil.getMd5(hashString);

    // Build response with all PayHere required parameters
    PaymentInitiationResponseDto response = new PaymentInitiationResponseDto();
    response.setMerchantId(payHereConfig.getMerchantId());
    response.setOrderId(dto.getOrderId());
    // Use the formatted amount (2 decimal places) to match the hash
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

    // TODO: Create Payment entity with PENDING status and save to database

    return response;
  }
}