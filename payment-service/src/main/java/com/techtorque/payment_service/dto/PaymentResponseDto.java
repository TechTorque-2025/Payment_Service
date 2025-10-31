package com.techtorque.payment_service.dto;

import com.techtorque.payment_service.entity.PaymentMethod;
import com.techtorque.payment_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private String id;
    private String invoiceId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String paymentGatewayTransactionId;
    private LocalDateTime createdAt;
}
