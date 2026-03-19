package com.project.evgo.booking;

import com.project.evgo.booking.response.AvailableSlotResponse;
import com.project.evgo.booking.response.CalendarStatusResponse;
import com.project.evgo.booking.response.DurationConfigResponse;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface BookingMetadataService {

    DurationConfigResponse getDurations();

    List<CalendarStatusResponse> getCalendarStatus(Long stationId, YearMonth month);

    List<AvailableSlotResponse> getAvailableSlots(Long stationId, LocalDate date, Double durationHour);
}
