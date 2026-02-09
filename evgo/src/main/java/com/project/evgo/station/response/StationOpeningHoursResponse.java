package com.project.evgo.station.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Builder;

/**
 * Response DTO for station opening hours.
 */
@Builder
public record StationOpeningHoursResponse(
	Long id,
	DayOfWeek dayOfWeek,
	LocalTime openTime,
	LocalTime closeTime,
	Boolean isOpen) {
}
