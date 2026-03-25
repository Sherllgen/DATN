package com.project.evgo.booking;

import java.math.BigDecimal;

/**
 * Event published when a new booking is created (pending payment).
 */
public record BookingCreatedEvent(
        Long bookingId,
        Long userId,
        BigDecimal totalPrice
) {
}
