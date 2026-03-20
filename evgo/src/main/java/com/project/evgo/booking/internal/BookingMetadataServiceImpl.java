package com.project.evgo.booking.internal;

import com.project.evgo.booking.BookingMetadataService;
import com.project.evgo.booking.response.AvailableSlotResponse;
import com.project.evgo.booking.response.CalendarStatusResponse;
import com.project.evgo.booking.response.DurationConfigResponse;
import com.project.evgo.sharedkernel.enums.AvailabilityStatus;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.station.PortCountProvider;
import com.project.evgo.station.PortCounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingMetadataServiceImpl implements BookingMetadataService {

    private final BookingRepository bookingRepository;
    private final PortCountProvider portCountProvider;

    @Override
    public DurationConfigResponse getDurations() {
        List<Double> durations = new ArrayList<>();
        for (double d = 1.0; d <= 12.0; d += 0.5) {
            durations.add(d);
        }
        return new DurationConfigResponse(durations);
    }

    @Override
    public List<CalendarStatusResponse> getCalendarStatus(Long stationId, YearMonth month) {
        PortCounts portCounts = portCountProvider.getPortCounts(stationId);
        int totalPorts = portCounts.totalPorts();
        
        List<CalendarStatusResponse> response = new ArrayList<>();
        int daysInMonth = month.lengthOfMonth();
        
        if (totalPorts == 0) {
            for (int day = 1; day <= daysInMonth; day++) {
                response.add(new CalendarStatusResponse(month.atDay(day), AvailabilityStatus.CLOSED));
            }
            return response;
        }

        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);
        List<Booking> bookings = bookingRepository.findByStationIdAndStatusInAndStartTimeBetween(stationId, activeStatuses, startOfMonth, endOfMonth);

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = month.atDay(day);
            AvailabilityStatus status = calculateDayStatus(totalPorts, date, bookings);
            response.add(new CalendarStatusResponse(date, status));
        }

        return response;
    }

    private AvailabilityStatus calculateDayStatus(int totalPorts, LocalDate date, List<Booking> monthBookings) {
        List<Booking> dayBookings = monthBookings.stream()
                .filter(b -> b.getStartTime().toLocalDate().equals(date) || b.getEndTime().toLocalDate().equals(date))
                .toList();

        if (dayBookings.isEmpty()) {
            return AvailabilityStatus.AVAILABLE;
        }

        List<AvailableSlotResponse> slots = calculateSlots(totalPorts, date, 1.0, dayBookings);
        boolean hasAvailableSlot = slots.stream().anyMatch(slot -> slot.getAvailablePorts() > 0);
        return hasAvailableSlot ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.FULL;
    }

    @Override
    public List<AvailableSlotResponse> getAvailableSlots(Long stationId, LocalDate date, Double durationHour) {
        PortCounts portCounts = portCountProvider.getPortCounts(stationId);
        int totalPorts = portCounts.totalPorts();
        
        if (totalPorts == 0) {
            return Collections.emptyList();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);
        List<Booking> bookings = bookingRepository.findByStationIdAndStatusInAndStartTimeBetween(stationId, activeStatuses, startOfDay, endOfDay);

        return calculateSlots(totalPorts, date, durationHour, bookings);
    }

    private List<AvailableSlotResponse> calculateSlots(int totalPorts, LocalDate date, Double durationHour, List<Booking> bookings) {
        List<AvailableSlotResponse> slots = new ArrayList<>();
        long durationMinutes = (long) (durationHour * 60);
        
        LocalTime currentTime = LocalTime.of(0, 0);
        
        while (true) {
            long currentMinutes = currentTime.getHour() * 60 + currentTime.getMinute();
            if (currentMinutes + durationMinutes > 24 * 60) {
                break;
            }
            
            LocalTime slotEnd;
            LocalDateTime slotEndDateTime;
            if (currentMinutes + durationMinutes == 24 * 60) {
                slotEnd = LocalTime.of(0, 0);
                slotEndDateTime = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 0));
            } else {
                slotEnd = currentTime.plusMinutes(durationMinutes);
                slotEndDateTime = LocalDateTime.of(date, slotEnd);
            }
            
            LocalDateTime slotStartDateTime = LocalDateTime.of(date, currentTime);

            int maxConcurrent = 0;
            LocalDateTime intervalStart = slotStartDateTime;
            while (intervalStart.isBefore(slotEndDateTime)) {
                LocalDateTime intervalEnd = intervalStart.plusMinutes(30);
                int concurrentBookings = 0;
                for (Booking booking : bookings) {
                    if (booking.getStartTime().isBefore(intervalEnd) && booking.getEndTime().isAfter(intervalStart)) {
                        concurrentBookings++;
                    }
                }
                if (concurrentBookings > maxConcurrent) {
                    maxConcurrent = concurrentBookings;
                }
                intervalStart = intervalStart.plusMinutes(30);
            }
            
            int availablePorts = Math.max(0, totalPorts - maxConcurrent);
            slots.add(new AvailableSlotResponse(currentTime, slotEnd, availablePorts));
            
            if (currentMinutes + 30 >= 24 * 60) {
                break;
            }
            currentTime = currentTime.plusMinutes(30);
        }
        
        return slots;
    }
}
