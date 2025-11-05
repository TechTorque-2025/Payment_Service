package com.techtorque.payment_service.service;

import com.techtorque.payment_service.dto.request.CreateInvoiceDto;
import com.techtorque.payment_service.dto.request.PaymentRequestDto;
import com.techtorque.payment_service.dto.request.SchedulePaymentDto;
import com.techtorque.payment_service.dto.response.InvoiceResponseDto;
import com.techtorque.payment_service.dto.response.PaymentResponseDto;
import com.techtorque.payment_service.dto.response.ScheduledPaymentResponseDto;
import com.techtorque.payment_service.dto.PaymentInitiationDto;
import com.techtorque.payment_service.dto.PaymentInitiationResponseDto;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface BillingService {

  // Invoice operations
  InvoiceResponseDto createInvoice(CreateInvoiceDto dto);
  
  InvoiceResponseDto createInvoiceForService(String serviceId, String customerId, CreateInvoiceDto dto);

  InvoiceResponseDto getInvoiceById(String invoiceId, String userId);
  
  List<InvoiceResponseDto> listInvoicesForCustomer(String customerId);

  void sendInvoice(String invoiceId, String email);

  // Payment operations
  PaymentResponseDto processPayment(PaymentRequestDto dto, String customerId);

  List<PaymentResponseDto> getPaymentHistoryForCustomer(String customerId);

  PaymentResponseDto getPaymentDetails(String paymentId, String userId);

  // Scheduled payment operations
  ScheduledPaymentResponseDto schedulePayment(SchedulePaymentDto dto, String customerId);
  
  List<ScheduledPaymentResponseDto> getScheduledPaymentsForCustomer(String customerId);

  // PayHere integration
  PaymentInitiationResponseDto initiatePayHerePayment(PaymentInitiationDto dto);
  
  void verifyAndProcessNotification(MultiValueMap<String, String> formData);
}