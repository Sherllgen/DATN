package com.project.evgo.sharedkernel.enums;

/**
 * Status of an invoice.
 */
public enum InvoiceStatus {
    PENDING, // Awaiting payment
    PAID, // Payment completed
    CANCELLED, // Invoice cancelled
    REFUNDED // Payment refunded
}
