package com.techtorque.payment_service.service;

import com.techtorque.payment_service.dto.notification.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Client service for sending notifications to Notification Service
 * Handles payment-related notifications (invoice created, payment due, payment received)
 */
@Service
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification.service.url:http://localhost:8088}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    /**
     * Send notification to user asynchronously
     * Non-blocking - failures won't affect payment operations
     */
    public void sendNotification(NotificationRequest request) {
        try {
            log.info("Sending payment notification to user: {} - {}", request.getUserId(), request.getMessage());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                notificationServiceUrl + "/api/v1/notifications/create",
                entity,
                Void.class
            );

            log.debug("Payment notification sent successfully");

        } catch (Exception e) {
            // Log error but don't throw - notification failure shouldn't break payment operations
            log.error("Failed to send payment notification to user {}: {}", request.getUserId(), e.getMessage());
        }
    }

    /**
     * Helper method to send invoice notification
     */
    public void sendInvoiceNotification(String userId, String type, String message,
                                        String details, String invoiceId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .type(type)
            .message(message)
            .details(details)
            .relatedEntityId(invoiceId)
            .relatedEntityType("INVOICE")
            .build();

        sendNotification(request);
    }

    /**
     * Helper method to send payment notification
     */
    public void sendPaymentNotification(String userId, String type, String message,
                                        String details, String paymentId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .type(type)
            .message(message)
            .details(details)
            .relatedEntityId(paymentId)
            .relatedEntityType("PAYMENT")
            .build();

        sendNotification(request);
    }
}
