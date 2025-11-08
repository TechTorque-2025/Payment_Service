package com.techtorque.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceDto {
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotBlank(message = "Service or project ID is required")
    private String serviceOrProjectId;
    
    @NotEmpty(message = "Invoice must have at least one item")
    private List<InvoiceItemRequest> items;
    
    private LocalDate dueDate;
    
    private String notes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemRequest {
        @NotBlank(message = "Item description is required")
        private String description;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;
        
        @NotBlank(message = "Item type is required")
        private String itemType; // LABOR, PARTS, SERVICE_FEE, etc.
    }
}
