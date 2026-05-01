package com.project.evgo.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Booking module statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsResponse {
    private long totalBookings;
    private long totalCustomers;
    private double bookingsGrowth;
    private double customersGrowth;
}
