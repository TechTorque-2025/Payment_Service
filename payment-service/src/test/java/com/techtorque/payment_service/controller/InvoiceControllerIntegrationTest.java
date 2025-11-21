package com.techtorque.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtorque.payment_service.dto.request.CreateInvoiceDto;
import com.techtorque.payment_service.dto.request.SendInvoiceDto;
import com.techtorque.payment_service.dto.response.InvoiceResponseDto;
import com.techtorque.payment_service.entity.InvoiceStatus;
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
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    private InvoiceResponseDto testInvoiceResponse;

    @BeforeEach
    void setUp() {
        testInvoiceResponse = InvoiceResponseDto.builder()
                .invoiceId("invoice123")
                .invoiceNumber("INV-12345678")
                .customerId("customer123")
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .serviceId("service456")
                .items(new ArrayList<>())
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1000.00"))
                .paidAmount(BigDecimal.ZERO)
                .balanceAmount(new BigDecimal("1000.00"))
                .status(InvoiceStatus.SENT)
                .dueDate(LocalDate.now().plusDays(30))
                .issuedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .requiresDeposit(false)
                .build();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testCreateInvoice_Success() throws Exception {
        CreateInvoiceDto request = new CreateInvoiceDto();
        request.setCustomerId("customer123");
        request.setServiceOrProjectId("service456");
        request.setDueDate(LocalDate.now().plusDays(30));
        request.setNotes("Test invoice");
        request.setRequiresDeposit(false);

        CreateInvoiceDto.InvoiceItemRequest itemRequest = new CreateInvoiceDto.InvoiceItemRequest();
        itemRequest.setDescription("Labor");
        itemRequest.setQuantity(10);
        itemRequest.setUnitPrice(new BigDecimal("100.00"));
        itemRequest.setItemType("LABOR");

        request.setItems(Arrays.asList(itemRequest));

        when(billingService.createInvoice(any(CreateInvoiceDto.class)))
                .thenReturn(testInvoiceResponse);

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceId").value("invoice123"))
                .andExpect(jsonPath("$.totalAmount").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetInvoice_Success() throws Exception {
        when(billingService.getInvoiceById("invoice123", "customer123"))
                .thenReturn(testInvoiceResponse);

        mockMvc.perform(get("/invoices/{invoiceId}", "invoice123")
                        .header("X-User-Subject", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value("invoice123"))
                .andExpect(jsonPath("$.customerId").value("customer123"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testListInvoices_AsCustomer() throws Exception {
        when(billingService.listInvoicesForCustomer("customer123"))
                .thenReturn(Arrays.asList(testInvoiceResponse));

        mockMvc.perform(get("/invoices")
                        .header("X-User-Subject", "customer123")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].invoiceId").value("invoice123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListInvoices_AsAdmin() throws Exception {
        when(billingService.listAllInvoices())
                .thenReturn(Arrays.asList(testInvoiceResponse));

        mockMvc.perform(get("/invoices")
                        .header("X-User-Subject", "admin123")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].invoiceId").value("invoice123"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testSendInvoice_Success() throws Exception {
        SendInvoiceDto request = new SendInvoiceDto();
        request.setEmail("customer@example.com");

        doNothing().when(billingService).sendInvoice(anyString(), anyString());

        mockMvc.perform(post("/invoices/{invoiceId}/send", "invoice123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invoice sent successfully"));
    }
}
