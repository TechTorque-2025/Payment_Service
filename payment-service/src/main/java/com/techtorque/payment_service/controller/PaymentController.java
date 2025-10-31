package com.techtorque.payment_service.controller;

import com.techtorque.payment_service.dto.ApiResponse;
import com.techtorque.payment_service.dto.PaymentRequestDto;
import com.techtorque.payment_service.dto.PaymentResponseDto;
import com.techtorque.payment_service.entity.Payment;
import com.techtorque.payment_service.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpoints for processing and viewing payments.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PaymentController {

  private final BillingService billingService;

  @Operation(summary = "Callback endpoint for PayHere to send payment notifications (webhook)")
  @PostMapping(path = "/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @PreAuthorize("permitAll()") // This endpoint must be public to receive the webhook
  public ResponseEntity<ApiResponse> handlePayhereNotification(@RequestParam MultiValueMap<String, String> formData) {
    billingService.verifyAndProcessNotification(formData);
    return ResponseEntity.ok(ApiResponse.success("Notification processed"));
  }

  @Operation(summary = "Process a new payment for an invoice")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> processPayment(
          @Valid @RequestBody PaymentRequestDto dto,
          @RequestHeader("X-User-Subject") String customerId) {

    Payment payment = billingService.processPayment(dto, customerId);
    PaymentResponseDto response = mapToResponseDto(payment);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Payment processed successfully", response));
  }

  @Operation(summary = "Get the payment history for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  @ResponseBody
  public ResponseEntity<ApiResponse> getPaymentHistory(
          @RequestHeader("X-User-Subject") String customerId) {

    List<Payment> payments = billingService.getPaymentHistoryForCustomer(customerId);
    List<PaymentResponseDto> response = payments.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully", response));
  }

  @Operation(summary = "Get details for a specific payment")
  @GetMapping("/{paymentId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<ApiResponse> getPaymentDetails(@PathVariable String paymentId) {

    Payment payment = billingService.getPaymentDetails(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    PaymentResponseDto response = mapToResponseDto(payment);
    return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", response));
  }

  // Helper method to map Entity to DTO
  private PaymentResponseDto mapToResponseDto(Payment payment) {
    return PaymentResponseDto.builder()
            .id(payment.getId())
            .invoiceId(payment.getInvoiceId())
            .amount(payment.getAmount())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .paymentGatewayTransactionId(payment.getPaymentGatewayTransactionId())
            .createdAt(payment.getCreatedAt())
            .build();
  }
}