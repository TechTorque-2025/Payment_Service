package com.techtorque.payment_service.service;

import com.techtorque.payment_service.config.PayHereConfig;
import com.techtorque.payment_service.dto.request.CreateInvoiceDto;
import com.techtorque.payment_service.dto.request.PaymentRequestDto;
import com.techtorque.payment_service.dto.request.SchedulePaymentDto;
import com.techtorque.payment_service.dto.response.InvoiceResponseDto;
import com.techtorque.payment_service.dto.response.PaymentResponseDto;
import com.techtorque.payment_service.dto.response.ScheduledPaymentResponseDto;
import com.techtorque.payment_service.entity.*;
import com.techtorque.payment_service.exception.InvoiceNotFoundException;
import com.techtorque.payment_service.exception.InvalidPaymentException;
import com.techtorque.payment_service.exception.PaymentNotFoundException;
import com.techtorque.payment_service.exception.UnauthorizedAccessException;
import com.techtorque.payment_service.repository.InvoiceItemRepository;
import com.techtorque.payment_service.repository.InvoiceRepository;
import com.techtorque.payment_service.repository.PaymentRepository;
import com.techtorque.payment_service.repository.ScheduledPaymentRepository;
import com.techtorque.payment_service.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceItemRepository invoiceItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ScheduledPaymentRepository scheduledPaymentRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private PayHereConfig payHereConfig;

    @InjectMocks
    private BillingServiceImpl billingService;

    private Invoice testInvoice;
    private Payment testPayment;
    private ScheduledPayment testScheduledPayment;

    @BeforeEach
    void setUp() {
        testInvoice = Invoice.builder()
                .id("invoice123")
                .customerId("customer123")
                .serviceOrProjectId("service456")
                .amount(new BigDecimal("1000.00"))
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .requiresDeposit(false)
                .items(new ArrayList<>())
                .build();

        testPayment = Payment.builder()
                .id("payment123")
                .invoiceId("invoice123")
                .customerId("customer123")
                .amount(new BigDecimal("1000.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .build();

        testScheduledPayment = ScheduledPayment.builder()
                .id("scheduled123")
                .invoiceId("invoice123")
                .customerId("customer123")
                .amount(new BigDecimal("500.00"))
                .scheduledDate(LocalDate.now().plusDays(7))
                .status("SCHEDULED")
                .build();
    }

    @Test
    void testCreateInvoice_Success() {
        CreateInvoiceDto dto = new CreateInvoiceDto();
        dto.setCustomerId("customer123");
        dto.setServiceOrProjectId("service456");
        dto.setDueDate(LocalDate.now().plusDays(30));
        dto.setNotes("Test invoice");
        dto.setRequiresDeposit(false);

        CreateInvoiceDto.InvoiceItemRequest itemRequest = new CreateInvoiceDto.InvoiceItemRequest();
        itemRequest.setDescription("Labor");
        itemRequest.setQuantity(10);
        itemRequest.setUnitPrice(new BigDecimal("100.00"));
        itemRequest.setItemType("LABOR");

        dto.setItems(Arrays.asList(itemRequest));

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(invoiceItemRepository.save(any(InvoiceItem.class))).thenReturn(new InvoiceItem());
        when(invoiceItemRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());
        when(paymentRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());

        InvoiceResponseDto response = billingService.createInvoice(dto);

        assertThat(response).isNotNull();
        assertThat(response.getInvoiceId()).isEqualTo("invoice123");
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(invoiceItemRepository, times(1)).save(any(InvoiceItem.class));
    }

    @Test
    void testCreateInvoice_WithDepositRequirement() {
        CreateInvoiceDto dto = new CreateInvoiceDto();
        dto.setCustomerId("customer123");
        dto.setServiceOrProjectId("service456");
        dto.setDueDate(LocalDate.now().plusDays(30));
        dto.setRequiresDeposit(true);

        CreateInvoiceDto.InvoiceItemRequest itemRequest = new CreateInvoiceDto.InvoiceItemRequest();
        itemRequest.setDescription("Labor");
        itemRequest.setQuantity(10);
        itemRequest.setUnitPrice(new BigDecimal("100.00"));
        itemRequest.setItemType("LABOR");

        dto.setItems(Arrays.asList(itemRequest));

        Invoice depositInvoice = Invoice.builder()
                .id("invoice123")
                .customerId("customer123")
                .serviceOrProjectId("service456")
                .amount(new BigDecimal("1000.00"))
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .requiresDeposit(true)
                .depositAmount(new BigDecimal("500.00"))
                .finalAmount(new BigDecimal("500.00"))
                .items(new ArrayList<>())
                .build();

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(depositInvoice);
        when(invoiceItemRepository.save(any(InvoiceItem.class))).thenReturn(new InvoiceItem());
        when(invoiceItemRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());
        when(paymentRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());

        InvoiceResponseDto response = billingService.createInvoice(dto);

        assertThat(response).isNotNull();
        assertThat(response.getRequiresDeposit()).isTrue();
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testGetInvoiceById_Success() {
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(invoiceItemRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());
        when(paymentRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());

        InvoiceResponseDto response = billingService.getInvoiceById("invoice123", "customer123");

        assertThat(response).isNotNull();
        assertThat(response.getInvoiceId()).isEqualTo("invoice123");
        verify(invoiceRepository, times(1)).findById("invoice123");
    }

    @Test
    void testGetInvoiceById_NotFound() {
        when(invoiceRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.getInvoiceById("nonexistent", "customer123"))
                .isInstanceOf(InvoiceNotFoundException.class)
                .hasMessageContaining("Invoice not found");
    }

    @Test
    void testGetInvoiceById_UnauthorizedAccess() {
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() -> billingService.getInvoiceById("invoice123", "wrongCustomer"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("does not have access");
    }

    @Test
    void testListInvoicesForCustomer() {
        when(invoiceRepository.findByCustomerIdOrderByCreatedAtDesc("customer123"))
                .thenReturn(Arrays.asList(testInvoice));
        when(invoiceItemRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());
        when(paymentRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());

        List<InvoiceResponseDto> responses = billingService.listInvoicesForCustomer("customer123");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCustomerId()).isEqualTo("customer123");
        verify(invoiceRepository, times(1)).findByCustomerIdOrderByCreatedAtDesc("customer123");
    }

    @Test
    void testListAllInvoices() {
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));
        when(invoiceItemRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());
        when(paymentRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());

        List<InvoiceResponseDto> responses = billingService.listAllInvoices();

        assertThat(responses).hasSize(1);
        verify(invoiceRepository, times(1)).findAll();
    }

    @Test
    void testSendInvoice_Success() {
        Invoice draftInvoice = Invoice.builder()
                .id("invoice123")
                .customerId("customer123")
                .serviceOrProjectId("service456")
                .amount(new BigDecimal("1000.00"))
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .requiresDeposit(false)
                .items(new ArrayList<>())
                .build();

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(draftInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(draftInvoice);

        billingService.sendInvoice("invoice123", "customer@example.com");

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testProcessPayment_Success() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setInvoiceId("invoice123");
        dto.setAmount(new BigDecimal("1000.00"));
        dto.setMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentRepository.findByInvoiceId(anyString())).thenReturn(Arrays.asList(testPayment));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        PaymentResponseDto response = billingService.processPayment(dto, "customer123");

        assertThat(response).isNotNull();
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testProcessPayment_InvoiceNotFound() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setInvoiceId("nonexistent");
        dto.setAmount(new BigDecimal("1000.00"));
        dto.setMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.processPayment(dto, "customer123"))
                .isInstanceOf(InvoiceNotFoundException.class)
                .hasMessageContaining("Invoice not found");
    }

    @Test
    void testProcessPayment_UnauthorizedAccess() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setInvoiceId("invoice123");
        dto.setAmount(new BigDecimal("1000.00"));
        dto.setMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() -> billingService.processPayment(dto, "wrongCustomer"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("does not have access");
    }

    @Test
    void testProcessPayment_InvalidAmount_TooHigh() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setInvoiceId("invoice123");
        dto.setAmount(new BigDecimal("2000.00"));
        dto.setMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() -> billingService.processPayment(dto, "customer123"))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("exceeds invoice amount");
    }

    @Test
    void testProcessPayment_InvalidAmount_Negative() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setInvoiceId("invoice123");
        dto.setAmount(new BigDecimal("-100.00"));
        dto.setMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() -> billingService.processPayment(dto, "customer123"))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    void testGetPaymentHistoryForCustomer() {
        when(paymentRepository.findByCustomerIdOrderByCreatedAtDesc("customer123"))
                .thenReturn(Arrays.asList(testPayment));

        List<PaymentResponseDto> responses = billingService.getPaymentHistoryForCustomer("customer123");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getPaymentId()).isEqualTo("payment123");
        verify(paymentRepository, times(1)).findByCustomerIdOrderByCreatedAtDesc("customer123");
    }

    @Test
    void testGetPaymentDetails_Success() {
        when(paymentRepository.findById("payment123")).thenReturn(Optional.of(testPayment));

        PaymentResponseDto response = billingService.getPaymentDetails("payment123", "customer123");

        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo("payment123");
        verify(paymentRepository, times(1)).findById("payment123");
    }

    @Test
    void testGetPaymentDetails_NotFound() {
        when(paymentRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.getPaymentDetails("nonexistent", "customer123"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void testGetPaymentDetails_UnauthorizedAccess() {
        when(paymentRepository.findById("payment123")).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> billingService.getPaymentDetails("payment123", "wrongCustomer"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("does not have access");
    }

    @Test
    void testSchedulePayment_Success() {
        SchedulePaymentDto dto = new SchedulePaymentDto();
        dto.setInvoiceId("invoice123");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setScheduledDate(LocalDate.now().plusDays(7));
        dto.setNotes("Scheduled payment");

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(scheduledPaymentRepository.save(any(ScheduledPayment.class))).thenReturn(testScheduledPayment);

        ScheduledPaymentResponseDto response = billingService.schedulePayment(dto, "customer123");

        assertThat(response).isNotNull();
        assertThat(response.getScheduleId()).isEqualTo("scheduled123");
        verify(scheduledPaymentRepository, times(1)).save(any(ScheduledPayment.class));
    }

    @Test
    void testSchedulePayment_InvoiceNotFound() {
        SchedulePaymentDto dto = new SchedulePaymentDto();
        dto.setInvoiceId("nonexistent");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setScheduledDate(LocalDate.now().plusDays(7));

        when(invoiceRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.schedulePayment(dto, "customer123"))
                .isInstanceOf(InvoiceNotFoundException.class)
                .hasMessageContaining("Invoice not found");
    }

    @Test
    void testSchedulePayment_UnauthorizedAccess() {
        SchedulePaymentDto dto = new SchedulePaymentDto();
        dto.setInvoiceId("invoice123");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setScheduledDate(LocalDate.now().plusDays(7));

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() -> billingService.schedulePayment(dto, "wrongCustomer"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("does not have access");
    }

    @Test
    void testSchedulePayment_InvalidAmount() {
        SchedulePaymentDto dto = new SchedulePaymentDto();
        dto.setInvoiceId("invoice123");
        dto.setAmount(new BigDecimal("2000.00"));
        dto.setScheduledDate(LocalDate.now().plusDays(7));

        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() -> billingService.schedulePayment(dto, "customer123"))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("exceeds invoice amount");
    }

    @Test
    void testGetScheduledPaymentsForCustomer() {
        when(scheduledPaymentRepository.findByCustomerId("customer123"))
                .thenReturn(Arrays.asList(testScheduledPayment));

        List<ScheduledPaymentResponseDto> responses = billingService.getScheduledPaymentsForCustomer("customer123");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getScheduleId()).isEqualTo("scheduled123");
        verify(scheduledPaymentRepository, times(1)).findByCustomerId("customer123");
    }
}
