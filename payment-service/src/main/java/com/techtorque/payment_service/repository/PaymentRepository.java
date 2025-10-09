package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
  List<Payment> findByInvoiceId(String invoiceId);
}