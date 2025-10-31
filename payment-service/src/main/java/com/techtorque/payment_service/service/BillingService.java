package com.techtorque.payment_service.service;

import com.techtorque.payment_service.dto.InvoiceRequestDto;
import com.techtorque.payment_service.dto.PaymentRequestDto;
import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.Payment;
import org.springframework.util.MultiValueMap;
import java.util.List;
import java.util.Optional;

public interface BillingService {

  Invoice createInvoice(InvoiceRequestDto dto);

  Invoice getInvoiceById(String invoiceId);

  Payment processPayment(PaymentRequestDto dto, String customerId);

  List<Payment> getPaymentHistoryForCustomer(String customerId);

  Optional<Payment> getPaymentDetails(String paymentId);

  List<Invoice> listInvoicesForCustomer(String customerId);

  void sendInvoice(String invoiceId, String email);

  Invoice markInvoiceAsPaid(String invoiceId);

  void verifyAndProcessNotification(MultiValueMap<String, String> formData);
}