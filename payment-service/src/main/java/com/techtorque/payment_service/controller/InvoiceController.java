package com.techtorque.payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "Endpoints for managing invoices.")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

  // @Autowired
  // private BillingService billingService;

  @Operation(summary = "List invoices for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> listInvoices(
          @RequestHeader("X-User-Subject") String customerId) {
    // TODO: Delegate to billingService.listInvoicesForCustomer(customerId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Send an invoice to a customer via email (employee/admin only)")
  @PostMapping("/{invoiceId}/send")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<?> sendInvoiceByEmail(
          @PathVariable String invoiceId
          /* @RequestBody SendInvoiceDto dto */) {
    // TODO: Delegate to billingService.sendInvoice(invoiceId, dto.getEmail());
    return ResponseEntity.ok().build();
  }
}