package com.techtorque.payment_service.service;

import com.techtorque.payment_service.dto.PaymentInitiationDto;
import com.techtorque.payment_service.dto.PaymentInitiationResponseDto;
import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.Payment;
import org.springframework.util.MultiValueMap; // Import this
import java.util.List;
import java.util.Optional;

public interface BillingService {

  Invoice createInvoiceForService(String serviceId, String customerId /*, other details */);

  Payment processPayment(/* PaymentRequestDto dto, */ String customerId);

  List<Payment> getPaymentHistoryForCustomer(String customerId);

  Optional<Payment> getPaymentDetails(String paymentId, String userId);

  Object schedulePayment(/* SchedulePaymentDto dto, */ String customerId);

  List<Invoice> listInvoicesForCustomer(String customerId);

  void sendInvoice(String invoiceId, String email);

  Object initiatePayment(String invoiceId);

  void verifyAndProcessNotification(MultiValueMap<String, String> formData);

  PaymentInitiationResponseDto initiatePayHerePayment(PaymentInitiationDto dto);
}