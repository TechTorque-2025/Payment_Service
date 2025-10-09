package com.techtorque.payment_service.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments & Billing", description = "Endpoints for processing payments and managing invoices.")
public class PaymentController {

  // @Autowired
  // private BillingService billingService;

  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> processPayment(
          /* @RequestBody PaymentRequestDto dto, */
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to billingService.processPayment(dto, customerId);
    return ResponseEntity.ok().build();
  }

  // Other endpoints for invoices can be in an InvoiceController
}