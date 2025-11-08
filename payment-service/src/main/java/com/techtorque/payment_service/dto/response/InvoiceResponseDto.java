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
    private String customerId;
    private String serviceOrProjectId;
    private BigDecimal amount;
    private InvoiceStatus status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InvoiceItemDto> items;
    private BigDecimal totalPaid;
    private BigDecimal balance;
}
