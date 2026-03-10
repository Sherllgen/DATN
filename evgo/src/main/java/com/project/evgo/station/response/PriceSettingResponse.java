package com.project.evgo.station.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for price setting information.
 * Public API - accessible by other modules.
 */
@Builder
public record PriceSettingResponse(
        Long id,
        Long stationId,
        Integer version,
        BigDecimal chargingRatePerKwh,
        BigDecimal bookingFee,
        BigDecimal idlePenaltyPerMinute,
        Integer gracePeriodMinutes,
        Boolean isActive,
        String notes,
        LocalDateTime effectiveFrom,
        LocalDateTime createdAt) {
}
