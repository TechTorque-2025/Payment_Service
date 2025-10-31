package com.techtorque.payment_service.controller;

import com.techtorque.payment_service.dto.ApiResponse;
import com.techtorque.payment_service.dto.InvoiceRequestDto;
import com.techtorque.payment_service.dto.InvoiceResponseDto;
import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "Endpoints for managing invoices.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class InvoiceController {

  private final BillingService billingService;

  @Operation(summary = "Create a new invoice (employee/admin only)")
  @PostMapping
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> createInvoice(@Valid @RequestBody InvoiceRequestDto dto) {

    Invoice invoice = billingService.createInvoice(dto);
    InvoiceResponseDto response = mapToResponseDto(invoice);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Invoice created successfully", response));
  }

  @Operation(summary = "List invoices for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> listInvoices(
          @RequestHeader("X-User-Subject") String customerId) {

    List<Invoice> invoices = billingService.listInvoicesForCustomer(customerId);
    List<InvoiceResponseDto> response = invoices.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", response));
  }

  @Operation(summary = "Get invoice details")
  @GetMapping("/{invoiceId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> getInvoice(@PathVariable String invoiceId) {

    Invoice invoice = billingService.getInvoiceById(invoiceId);
    InvoiceResponseDto response = mapToResponseDto(invoice);

    return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", response));
  }

  @Operation(summary = "Mark invoice as paid (employee/admin only)")
  @PutMapping("/{invoiceId}/pay")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> markAsPaid(@PathVariable String invoiceId) {

    Invoice invoice = billingService.markInvoiceAsPaid(invoiceId);
    InvoiceResponseDto response = mapToResponseDto(invoice);

    return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid", response));
  }

  @Operation(summary = "Send an invoice to a customer via email (employee/admin only)")
  @PostMapping("/{invoiceId}/send")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<ApiResponse> sendInvoiceByEmail(
          @PathVariable String invoiceId,
          @RequestParam String email) {

    billingService.sendInvoice(invoiceId, email);
    return ResponseEntity.ok(ApiResponse.success("Invoice sent successfully"));
  }

  // Helper method to map Entity to DTO
  private InvoiceResponseDto mapToResponseDto(Invoice invoice) {
    return InvoiceResponseDto.builder()
            .id(invoice.getId())
            .customerId(invoice.getCustomerId())
            .serviceOrProjectId(invoice.getServiceOrProjectId())
            .amount(invoice.getAmount())
            .status(invoice.getStatus())
            .issueDate(invoice.getIssueDate())
            .dueDate(invoice.getDueDate())
            .createdAt(invoice.getCreatedAt())
            .updatedAt(invoice.getUpdatedAt())
            .build();
  }
}