package com.techtorque.payment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank(message = "Invoice ID is required")
    @Column(nullable = false)
    private String invoiceId;
    
    @NotBlank(message = "Customer ID is required")
    @Column(nullable = false)
    private String customerId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Scheduled date is required")
    @Column(nullable = false)
    private LocalDate scheduledDate;
    
    @Column(nullable = false, length = 20)
    private String status; // SCHEDULED, PROCESSED, CANCELLED, FAILED
    
    @Column(length = 1000)
    private String notes;
    
    private String paymentId; // Link to actual payment once processed
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "SCHEDULED";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
