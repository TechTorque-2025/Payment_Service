package com.techtorque.payment_service.entity;

public enum InvoiceStatus {
  PENDING,
  DRAFT,
  SENT,
  PAID,
  PARTIALLY_PAID,
  VOID,
  OVERDUE,
  CANCELLED
}