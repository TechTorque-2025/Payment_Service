package com.techtorque.payment_service.exception;

public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
    
    public InvalidPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
