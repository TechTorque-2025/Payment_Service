package com.techtorque.payment_service.dto.response;

import com.techtorque.payment_service.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDto {
    private String invoiceId;
    private String invoiceNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String serviceId;
    private String projectId;
    private List<InvoiceItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private InvoiceStatus status;
    private String notes;
    private LocalDate dueDate;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
