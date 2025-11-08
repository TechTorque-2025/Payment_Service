package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.Payment;
import com.techtorque.payment_service.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
  
  List<Payment> findByInvoiceId(String invoiceId);
  
  List<Payment> findByCustomerId(String customerId);
  
  List<Payment> findByCustomerIdOrderByCreatedAtDesc(String customerId);
  
  List<Payment> findByStatus(PaymentStatus status);
  
  Optional<Payment> findByPaymentGatewayTransactionId(String transactionId);
  
  @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.status = :status")
  List<Payment> findByCustomerIdAndStatus(@Param("customerId") String customerId, 
                                          @Param("status") PaymentStatus status);
  
  @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
  List<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
  
  @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.createdAt BETWEEN :startDate AND :endDate")
  List<Payment> findCustomerPaymentsBetweenDates(@Param("customerId") String customerId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
}