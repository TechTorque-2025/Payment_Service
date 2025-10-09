package com.techtorque.payment_service.service;

import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.Payment;

public interface BillingService {
  Invoice createInvoiceForService(String serviceId, String customerId /*, other details */);
  Payment processPayment(/* PaymentRequestDto dto, */ String customerId);
}