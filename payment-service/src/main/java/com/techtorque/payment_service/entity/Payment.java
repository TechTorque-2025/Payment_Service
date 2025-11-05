package com.techtorque.payment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @NotBlank(message = "Invoice ID is required")
  @Column(nullable = false)
  private String invoiceId;
  
  @Column(nullable = false)
  private String customerId;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  private String paymentGatewayTransactionId; // e.g., from PayHere

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
  
  @Column(nullable = false)
  private LocalDateTime updatedAt;
  
  @Column(length = 1000)
  private String notes;
  
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) {
      status = PaymentStatus.PENDING;
    }
  }
  
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}