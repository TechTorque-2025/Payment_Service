package com.techtorque.payment_service.controller;

import com.techtorque.payment_service.dto.PaymentInitiationDto;
import com.techtorque.payment_service.dto.PaymentInitiationResponseDto;
import com.techtorque.payment_service.dto.request.PaymentRequestDto;
import com.techtorque.payment_service.dto.request.SchedulePaymentDto;
import com.techtorque.payment_service.dto.response.PaymentResponseDto;
import com.techtorque.payment_service.dto.response.ScheduledPaymentResponseDto;
import com.techtorque.payment_service.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpoints for processing and viewing payments.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

  @Autowired
  private BillingService billingService;

  @Operation(summary = "Initiate a payment and get required parameters for PayHere redirect")
  @PostMapping("/initiate")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<PaymentInitiationResponseDto> initiatePayment(
          @Valid @RequestBody PaymentInitiationDto dto) {
    PaymentInitiationResponseDto response = billingService.initiatePayHerePayment(dto);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Callback endpoint for PayHere to send payment notifications (webhook)")
  @PostMapping(path = "/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @PreAuthorize("permitAll()")
  public ResponseEntity<?> handlePayhereNotification(
          @RequestParam MultiValueMap<String, String> formData) {
    billingService.verifyAndProcessNotification(formData);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Process a new payment for an invoice")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<PaymentResponseDto> processPayment(
          @Valid @RequestBody PaymentRequestDto dto,
          @RequestHeader("X-User-Subject") String customerId) {
    PaymentResponseDto response = billingService.processPayment(dto, customerId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Get the payment history for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<List<PaymentResponseDto>> getPaymentHistory(
          @RequestHeader("X-User-Subject") String customerId) {
    List<PaymentResponseDto> payments = billingService.getPaymentHistoryForCustomer(customerId);
    return ResponseEntity.ok(payments);
  }

  @Operation(summary = "Get details for a specific payment")
  @GetMapping("/{paymentId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<PaymentResponseDto> getPaymentDetails(
          @PathVariable String paymentId,
          @RequestHeader("X-User-Subject") String userId) {
    PaymentResponseDto payment = billingService.getPaymentDetails(paymentId, userId);
    return ResponseEntity.ok(payment);
  }

  @Operation(summary = "Schedule a future payment for an invoice")
  @PostMapping("/schedule")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ScheduledPaymentResponseDto> schedulePayment(
          @Valid @RequestBody SchedulePaymentDto dto,
          @RequestHeader("X-User-Subject") String customerId) {
    ScheduledPaymentResponseDto response = billingService.schedulePayment(dto, customerId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Get scheduled payments for the current customer")
  @GetMapping("/schedule")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<List<ScheduledPaymentResponseDto>> getScheduledPayments(
          @RequestHeader("X-User-Subject") String customerId) {
    List<ScheduledPaymentResponseDto> scheduledPayments = 
        billingService.getScheduledPaymentsForCustomer(customerId);
    return ResponseEntity.ok(scheduledPayments);
  }
}