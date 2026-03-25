package com.project.evgo.booking;

import java.math.BigDecimal;

/**
 * Event published when a booking is cancelled but a delayed payment IPN confirms payment.
 * Requires an asynchronous refund process.
 */
public record RequireRefundEvent(
        Long bookingId,
        Long userId,
        BigDecimal amount,
        String reason
) {}
