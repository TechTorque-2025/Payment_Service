package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.ScheduledPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, String> {
    
    List<ScheduledPayment> findByCustomerId(String customerId);
    
    List<ScheduledPayment> findByInvoiceId(String invoiceId);
    
    List<ScheduledPayment> findByStatus(String status);
    
    @Query("SELECT sp FROM ScheduledPayment sp WHERE sp.scheduledDate = :date AND sp.status = 'SCHEDULED'")
    List<ScheduledPayment> findScheduledPaymentsForDate(LocalDate date);
    
    @Query("SELECT sp FROM ScheduledPayment sp WHERE sp.scheduledDate <= :date AND sp.status = 'SCHEDULED'")
    List<ScheduledPayment> findOverdueScheduledPayments(LocalDate date);
}
