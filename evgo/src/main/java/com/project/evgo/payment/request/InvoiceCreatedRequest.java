package com.project.evgo.payment.request;

import java.math.BigDecimal;

/**
 * Event published when a new booking is created (pending payment).
 */
public record InvoiceCreatedRequest(
        Long bookingId,
        Long userId,
        BigDecimal totalPrice
) {
}
