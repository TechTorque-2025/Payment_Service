package com.techtorque.payment_service.entity;

import jakarta.persistence.*;
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

  @Column(nullable = false)
  private String invoiceId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  private String paymentGatewayTransactionId; // e.g., from PayHere

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}