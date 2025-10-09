package com.techtorque.payment_service.entity;

public enum InvoiceStatus {
  DRAFT,
  SENT,
  PAID,
  PARTIALLY_PAID,
  VOID,
  OVERDUE
}