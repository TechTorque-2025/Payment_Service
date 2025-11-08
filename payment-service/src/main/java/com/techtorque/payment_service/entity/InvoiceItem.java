package com.techtorque.payment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @ToString.Exclude
    private Invoice invoice;
    
    @NotBlank(message = "Item description is required")
    @Column(nullable = false, length = 500)
    private String description;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @NotNull(message = "Total price is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @NotBlank(message = "Item type is required")
    @Column(nullable = false, length = 50)
    private String itemType; // LABOR, PARTS, SERVICE_FEE, TAX, DISCOUNT, etc.
    
    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
