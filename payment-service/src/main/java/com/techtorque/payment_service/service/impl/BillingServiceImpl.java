package com.techtorque.payment_service.service.impl;

import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.Payment;
import com.techtorque.payment_service.repository.InvoiceRepository;
import com.techtorque.payment_service.repository.PaymentRepository;
import com.techtorque.payment_service.service.BillingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient; // For external API calls

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

  private final InvoiceRepository invoiceRepository;
  private final PaymentRepository paymentRepository;
  private final WebClient.Builder webClientBuilder; // For calling PayHere

  public BillingServiceImpl(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, WebClient.Builder webClientBuilder) {
    this.invoiceRepository = invoiceRepository;
    this.paymentRepository = paymentRepository;
    this.webClientBuilder = webClientBuilder;
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
}