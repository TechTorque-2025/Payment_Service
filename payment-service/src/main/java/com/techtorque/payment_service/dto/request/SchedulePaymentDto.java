package com.techtorque.payment_service.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePaymentDto {
    
    @NotBlank(message = "Invoice ID is required")
    private String invoiceId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Scheduled date is required")
    @Future(message = "Scheduled date must be in the future")
    private LocalDate scheduledDate;
    
    private String notes;
}
