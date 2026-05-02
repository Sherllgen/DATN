package com.project.evgo.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing monthly revenue and booking count for chart display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyChartEntry {
    private String month;
    private long bookings;
    private BigDecimal revenue;
}
