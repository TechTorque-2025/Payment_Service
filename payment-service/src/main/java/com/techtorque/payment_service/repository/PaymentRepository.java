package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
  List<Payment> findByInvoiceId(String invoiceId);

  @Query("SELECT p FROM Payment p JOIN Invoice i ON p.invoiceId = i.id WHERE i.customerId = :customerId ORDER BY p.createdAt DESC")
  List<Payment> findPaymentsByCustomerId(String customerId);
}