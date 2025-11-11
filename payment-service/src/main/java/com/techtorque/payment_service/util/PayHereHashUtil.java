package com.techtorque.payment_service.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Base64;

public class PayHereHashUtil {

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate PayHere payment hash
     * Formula: MD5(merchant_id + order_id + amount + currency + MD5(merchant_secret))
     *
     * NOTE: If merchant_secret is Base64 encoded, it will be decoded first
     *
     * @param merchantId PayHere merchant ID
     * @param orderId Unique order ID
     * @param amount Payment amount (formatted to 2 decimal places)
     * @param currency Currency code (e.g., "LKR")
     * @param merchantSecret PayHere merchant secret (Base64 encoded or plain text)
     * @return Uppercase MD5 hash for PayHere payment
     */
    public static String generatePaymentHash(
            String merchantId,
            String orderId,
            BigDecimal amount,
            String currency,
            String merchantSecret) {

        // Format amount to 2 decimal places
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(amount);

        // Decode merchant secret from Base64 if it's encoded
        String decodedSecret = merchantSecret;
        try {
            // Try to decode as Base64
            byte[] decodedBytes = Base64.getDecoder().decode(merchantSecret);
            decodedSecret = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            // If decoding fails, use as-is (it's already plain text)
            decodedSecret = merchantSecret;
        }

        // Step 1: Hash the merchant secret
        String hashedSecret = getMd5(decodedSecret);

        // Step 2: Concatenate: merchant_id + order_id + amount + currency + hashed_secret
        String concatenated = merchantId + orderId + formattedAmount + currency + hashedSecret;

        // Step 3: Hash the concatenated string
        return getMd5(concatenated);
    }
}
