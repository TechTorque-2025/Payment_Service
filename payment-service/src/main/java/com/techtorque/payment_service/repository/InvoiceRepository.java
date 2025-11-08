package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.Invoice;
import com.techtorque.payment_service.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
  
  List<Invoice> findByCustomerId(String customerId);
  
  List<Invoice> findByCustomerIdOrderByCreatedAtDesc(String customerId);
  
  List<Invoice> findByStatus(InvoiceStatus status);
  
  // Use @Query to avoid property parsing issues with compound field names
  @Query("SELECT i FROM Invoice i WHERE i.serviceOrProjectId = :serviceOrProjectId")
  Optional<Invoice> findByServiceOrProjectId(@Param("serviceOrProjectId") String serviceOrProjectId);
  
  @Query("SELECT i FROM Invoice i WHERE i.customerId = :customerId AND i.status = :status")
  List<Invoice> findByCustomerIdAndStatus(@Param("customerId") String customerId, 
                                          @Param("status") InvoiceStatus status);
  
  @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status IN ('SENT', 'PARTIALLY_PAID')")
  List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);
  
  @Query("SELECT i FROM Invoice i WHERE i.customerId = :customerId AND i.dueDate < :date AND i.status IN ('SENT', 'PARTIALLY_PAID')")
  List<Invoice> findOverdueInvoicesByCustomer(@Param("customerId") String customerId, 
                                              @Param("date") LocalDate date);
  
  @Query("SELECT i FROM Invoice i WHERE i.issueDate BETWEEN :startDate AND :endDate")
  List<Invoice> findInvoicesBetweenDates(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
}