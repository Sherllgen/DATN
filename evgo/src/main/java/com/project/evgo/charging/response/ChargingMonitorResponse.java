package com.project.evgo.charging.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;

/**
 * SSE response DTO for real-time charging session monitoring.
 * Sent to the mobile client via Server-Sent Events during an active session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingMonitorResponse {

    private ChargingSessionStatus status;

    /** Energy consumed so far in kWh: (currentMeterValue - meterStart) / 1000. */
    private BigDecimal consumedKwh;

    /** Estimated cost so far: consumedKwh × chargingRatePerKwh. */
    private BigDecimal estimatedCost;

    private Integer currentMeterValue;

    private BigDecimal chargingRatePerKwh;

    private LocalDateTime timestamp;
}
