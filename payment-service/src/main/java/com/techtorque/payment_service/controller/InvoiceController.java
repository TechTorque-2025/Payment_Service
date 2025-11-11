package com.techtorque.payment_service.controller;

import com.techtorque.payment_service.dto.request.CreateInvoiceDto;
import com.techtorque.payment_service.dto.request.SendInvoiceDto;
import com.techtorque.payment_service.dto.response.InvoiceResponseDto;
import com.techtorque.payment_service.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "Endpoints for managing invoices.")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

  @Autowired
  private BillingService billingService;

  @Operation(summary = "Create a new invoice")
  @PostMapping
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<InvoiceResponseDto> createInvoice(
          @Valid @RequestBody CreateInvoiceDto dto) {
    InvoiceResponseDto invoice = billingService.createInvoice(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
  }

  @Operation(summary = "Get invoice by ID")
  @GetMapping("/{invoiceId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<InvoiceResponseDto> getInvoice(
          @PathVariable String invoiceId,
          @RequestHeader("X-User-Subject") String userId) {
    InvoiceResponseDto invoice = billingService.getInvoiceById(invoiceId, userId);
    return ResponseEntity.ok(invoice);
  }

  @Operation(summary = "List invoices for the current customer")
  @GetMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<InvoiceResponseDto>> listInvoices(
          @RequestHeader("X-User-Subject") String userId,
          @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {

    List<InvoiceResponseDto> invoices;

    // Admin, Super Admin and Employee can see all invoices, Customer sees only their own
    if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("SUPER_ADMIN") || userRoles.contains("EMPLOYEE"))) {
      invoices = billingService.listAllInvoices();
    } else {
      invoices = billingService.listInvoicesForCustomer(userId);
    }

    return ResponseEntity.ok(invoices);
  }

  @Operation(summary = "Send an invoice to a customer via email (employee/admin only)")
  @PostMapping("/{invoiceId}/send")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Map<String, String>> sendInvoiceByEmail(
          @PathVariable String invoiceId,
          @Valid @RequestBody SendInvoiceDto dto) {
    billingService.sendInvoice(invoiceId, dto.getEmail());
    return ResponseEntity.ok(Map.of("message", "Invoice sent successfully"));
  }
}