package com.project.evgo.booking;

import java.time.LocalDateTime;

/**
 * Sự kiện được phát ra SAU KHI một booking đã được thanh toán
 * và đã lưu (commit) thành công trạng thái CONFIRMED vào Database.
 */
public record BookingConfirmedAndReadyForHardwareEvent(
        Long bookingId,
        Long chargerId,
        Integer portNumber,
        Long userId,
        LocalDateTime endTime
) {
}
