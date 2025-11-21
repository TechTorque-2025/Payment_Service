package com.techtorque.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtorque.payment_service.dto.request.PaymentRequestDto;
import com.techtorque.payment_service.dto.request.SchedulePaymentDto;
import com.techtorque.payment_service.dto.response.PaymentResponseDto;
import com.techtorque.payment_service.dto.response.ScheduledPaymentResponseDto;
import com.techtorque.payment_service.entity.PaymentMethod;
import com.techtorque.payment_service.entity.PaymentStatus;
import com.techtorque.payment_service.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    private PaymentResponseDto testPaymentResponse;
    private ScheduledPaymentResponseDto testScheduledPaymentResponse;

    @BeforeEach
    void setUp() {
        testPaymentResponse = PaymentResponseDto.builder()
                .paymentId("payment123")
                .invoiceId("invoice123")
                .amount(new BigDecimal("1000.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        testScheduledPaymentResponse = ScheduledPaymentResponseDto.builder()
                .scheduleId("scheduled123")
                .invoiceId("invoice123")
                .customerId("customer123")
                .amount(new BigDecimal("500.00"))
                .scheduledDate(LocalDate.now().plusDays(7))
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testProcessPayment_Success() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto();
        request.setInvoiceId("invoice123");
        request.setAmount(new BigDecimal("1000.00"));
        request.setMethod(PaymentMethod.CASH);

        when(billingService.processPayment(any(PaymentRequestDto.class), anyString()))
                .thenReturn(testPaymentResponse);

        mockMvc.perform(post("/payments")
                        .header("X-User-Subject", "customer123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("payment123"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetPaymentHistory_Success() throws Exception {
        when(billingService.getPaymentHistoryForCustomer("customer123"))
                .thenReturn(Arrays.asList(testPaymentResponse));

        mockMvc.perform(get("/payments")
                        .header("X-User-Subject", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].paymentId").value("payment123"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetPaymentDetails_Success() throws Exception {
        when(billingService.getPaymentDetails("payment123", "customer123"))
                .thenReturn(testPaymentResponse);

        mockMvc.perform(get("/payments/{paymentId}", "payment123")
                        .header("X-User-Subject", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("payment123"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testSchedulePayment_Success() throws Exception {
        SchedulePaymentDto request = new SchedulePaymentDto();
        request.setInvoiceId("invoice123");
        request.setAmount(new BigDecimal("500.00"));
        request.setScheduledDate(LocalDate.now().plusDays(7));
        request.setNotes("Scheduled payment");

        when(billingService.schedulePayment(any(SchedulePaymentDto.class), anyString()))
                .thenReturn(testScheduledPaymentResponse);

        mockMvc.perform(post("/payments/schedule")
                        .header("X-User-Subject", "customer123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleId").value("scheduled123"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetScheduledPayments_Success() throws Exception {
        when(billingService.getScheduledPaymentsForCustomer("customer123"))
                .thenReturn(Arrays.asList(testScheduledPaymentResponse));

        mockMvc.perform(get("/payments/schedule")
                        .header("X-User-Subject", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].scheduleId").value("scheduled123"));
    }
}
