package com.techtorque.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationDto {
    private String invoiceId;  // Changed from orderId to match frontend
    private BigDecimal amount;
    private String currency;
    private String itemDescription;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerCity;
}
