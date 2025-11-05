package com.techtorque.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayHereNotificationDto {
    private String merchant_id;
    private String order_id;
    private String payhere_amount;
    private BigDecimal payhere_currency;
    private Integer status_code;
    private String md5sig;
    private String custom_1;
    private String custom_2;
    private String status_message;
    private String method;
    private String card_holder_name;
    private String card_no;
}
