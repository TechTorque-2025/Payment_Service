package com.techtorque.payment_service.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PayHereHashUtilTest {

    @Test
    public void testGeneratePaymentHash_WithPlainSecret() {
        String merchantId = "1231971";
        String orderId = "ORDER-ABC-123";
        BigDecimal amount = new BigDecimal("1000.00");
        String currency = "LKR";
        String merchantSecret = "plain-secret-123";

        // Generate expected: hashedSecret = MD5(merchantSecret)
        String hashedSecret = PayHereHashUtil.getMd5(merchantSecret);
        String input = merchantId + orderId + "1000.00" + currency + hashedSecret;
        String expected = PayHereHashUtil.getMd5(input);

        String actual = PayHereHashUtil.generatePaymentHash(merchantId, orderId, amount, currency, merchantSecret);

        assertEquals(expected, actual);
    }

    @Test
    public void testGeneratePaymentHash_WithBase64Secret() {
        String merchantId = "1231971";
        String orderId = "ORD-BASE64";
        BigDecimal amount = new BigDecimal("250.00");
        String currency = "LKR";

        // merchant secret base64 - for test use simple known string
        String plainSecret = "super-secret";
        String base64Secret = java.util.Base64.getEncoder().encodeToString(plainSecret.getBytes());

        // The util should decode base64 then MD5
        String hashedSecret = PayHereHashUtil.getMd5(plainSecret); // decode happens internally in generatePaymentHash
        String input = merchantId + orderId + "250.00" + currency + hashedSecret;
        String expected = PayHereHashUtil.getMd5(input);

        String actual = PayHereHashUtil.generatePaymentHash(merchantId, orderId, amount, currency, base64Secret);
        assertEquals(expected, actual);
    }
}
