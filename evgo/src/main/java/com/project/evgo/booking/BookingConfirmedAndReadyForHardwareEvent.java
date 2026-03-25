package com.project.evgo.booking;

import com.project.evgo.booking.internal.Booking;

/**
 * Sự kiện được phát ra SAU KHI một booking đã được thanh toán
 * và đã lưu (commit) thành công trạng thái CONFIRMED vào Database.
 */
public record BookingConfirmedAndReadyForHardwareEvent(Booking booking) {
}
