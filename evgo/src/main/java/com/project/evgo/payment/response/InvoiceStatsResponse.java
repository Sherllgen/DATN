package com.project.evgo.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Invoice module statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceStatsResponse {
    private BigDecimal totalRevenue;
    private double revenueGrowth;
}
