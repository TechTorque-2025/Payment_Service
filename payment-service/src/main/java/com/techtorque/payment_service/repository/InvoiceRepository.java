package com.techtorque.payment_service.repository;

import com.techtorque.payment_service.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
  List<Invoice> findByCustomerId(String customerId);
}