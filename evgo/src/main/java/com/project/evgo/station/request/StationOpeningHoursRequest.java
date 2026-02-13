package com.project.evgo.station.request;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Request DTO for station opening hours.
 */
public record StationOpeningHoursRequest(
	@NotNull(message = "Day of week is required") 
	DayOfWeek dayOfWeek,
	LocalTime openTime,
	LocalTime closeTime,
	Boolean isOpen) {
}
