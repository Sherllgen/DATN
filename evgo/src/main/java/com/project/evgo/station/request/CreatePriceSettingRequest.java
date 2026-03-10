package com.project.evgo.station.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a new price setting version.
 * Public API - accessible by other modules.
 */
public record CreatePriceSettingRequest(

	@NotNull(message = "Charging rate per kWh is required") 
	BigDecimal chargingRatePerKwh,

	BigDecimal bookingFee,

	BigDecimal idlePenaltyPerMinute,

	@Min(value = 0, message = "Grace period must be non-negative") 
	Integer gracePeriodMinutes,

	String notes,

	LocalDateTime effectiveFrom) {
}
