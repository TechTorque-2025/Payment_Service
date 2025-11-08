package com.techtorque.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPaymentResponseDto {
    private String scheduleId;
    private String invoiceId;
    private String customerId;
    private BigDecimal amount;
    private LocalDate scheduledDate;
    private String status; // SCHEDULED, PROCESSED, CANCELLED
    private String notes;
    private LocalDateTime createdAt;
}
