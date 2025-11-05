package com.techtorque.payment_service.controller;

import com.techtorque.payment_service.dto.PaymentInitiationDto;
import com.techtorque.payment_service.dto.PaymentInitiationResponseDto;
import com.techtorque.payment_service.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpoints for processing and viewing payments.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

  @Autowired
  private BillingService billingService;

  @Operation(summary = "Initiate a payment and get required parameters for PayHere redirect")
  @PostMapping("/initiate")
  // @PreAuthorize("hasRole('CUSTOMER')") // Temporarily disabled for debugging
  public ResponseEntity<PaymentInitiationResponseDto> initiatePayment(@RequestBody PaymentInitiationDto dto) {
    PaymentInitiationResponseDto response = billingService.initiatePayHerePayment(dto);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Callback endpoint for PayHere to send payment notifications (webhook)")
  @PostMapping(path = "/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @PreAuthorize("permitAll()") // This endpoint must be public to receive the webhook
  public ResponseEntity<?> handlePayhereNotification(@RequestParam MultiValueMap<String, String> formData) {
    billingService.verifyAndProcessNotification(formData);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Process a new payment for an invoice")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> processPayment(
          /* @RequestBody PaymentRequestDto dto, */
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to billingService.processPayment(dto, customerId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get the payment history for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> getPaymentHistory(
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to billingService.getPaymentHistoryForCustomer(customerId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get details for a specific payment")
  @GetMapping("/{paymentId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> getPaymentDetails(
          @PathVariable String paymentId,
          @RequestHeader("X-User-Subject") String userId) {
    // TODO: Delegate to billingService.getPaymentDetails(paymentId, userId);
    // Service layer must verify the user has permission to view this payment.
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Schedule a future payment for an invoice")
  @PostMapping("/schedule")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> schedulePayment(
          /* @RequestBody SchedulePaymentDto dto, */
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to billingService.schedulePayment(dto, customerId);
    return ResponseEntity.ok().build();
  }
}