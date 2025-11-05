package com.techtorque.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponseDto {
    private String merchantId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String hash;
    private String itemDescription;
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerCity;
    private boolean sandbox;
}
