package com.techtorque.payment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @NotBlank(message = "Customer ID is required")
  @Column(nullable = false)
  private String customerId;

  @NotBlank(message = "Service or project ID is required")
  @Column(nullable = false)
  private String serviceOrProjectId; // Link to the completed work

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InvoiceStatus status;

  @Column(nullable = false)
  private LocalDate issueDate;
  
  @Column(nullable = false)
  private LocalDate dueDate;
  
  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<InvoiceItem> items = new ArrayList<>();

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;
  
  @Column(length = 2000)
  private String notes;

  // Part-payment tracking
  @Column(nullable = false)
  @Builder.Default
  private Boolean requiresDeposit = false;

  @Column(precision = 10, scale = 2)
  private BigDecimal depositAmount; // 50% deposit

  @Column(precision = 10, scale = 2)
  private BigDecimal depositPaid;

  @Column
  private LocalDateTime depositPaidAt;

  @Column(precision = 10, scale = 2)
  private BigDecimal finalAmount; // 50% final payment

  @Column(precision = 10, scale = 2)
  private BigDecimal finalPaid;

  @Column
  private LocalDateTime finalPaidAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (issueDate == null) {
      issueDate = LocalDate.now();
    }
    if (dueDate == null) {
      dueDate = issueDate.plusDays(30); // Default 30 days payment term
    }
    if (status == null) {
      status = InvoiceStatus.DRAFT;
    }
  }
  
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
  
  // Helper method to add items
  public void addItem(InvoiceItem item) {
    items.add(item);
    item.setInvoice(this);
  }
  
  // Helper method to calculate total from items
  public BigDecimal calculateTotal() {
    return items.stream()
        .map(InvoiceItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}