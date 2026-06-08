package com.project.evgo.booking.internal;

import com.project.evgo.booking.BookingMetadataService;
import com.project.evgo.booking.response.AvailableSlotResponse;
import com.project.evgo.booking.response.CalendarStatusResponse;
import com.project.evgo.booking.response.DurationConfigResponse;
import com.project.evgo.sharedkernel.enums.AvailabilityStatus;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.station.PortCountProvider;
import com.project.evgo.station.PortCounts;
import com.project.evgo.station.StationService;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.station.response.StationOpeningHoursResponse;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingMetadataServiceImpl implements BookingMetadataService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private final BookingRepository bookingRepository;
    private final PortCountProvider portCountProvider;
    private final StationService stationService;

    /** Booking time range in VN local time (converted from UTC). */
    private record BookingTimeRange(LocalDateTime startTime, LocalDateTime endTime) {}

    private LocalDateTime utcToVn(LocalDateTime utcTime) {
        return utcTime.atZone(UTC_ZONE).withZoneSameInstant(VN_ZONE).toLocalDateTime();
    }

    private LocalDateTime vnToUtc(LocalDateTime vnTime) {
        return vnTime.atZone(VN_ZONE).withZoneSameInstant(UTC_ZONE).toLocalDateTime();
    }

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

        // Convert VN month range → UTC for DB query
        LocalDateTime vnStartOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime vnEndOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime utcStart = vnToUtc(vnStartOfMonth);
        LocalDateTime utcEnd = vnToUtc(vnEndOfMonth);

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED,
                BookingStatus.IN_PROGRESS);
        List<Booking> bookings = bookingRepository.findOverlappingBookings(stationId,
                activeStatuses, utcStart, utcEnd);

        // Convert booking times UTC → VN for slot comparison
        List<BookingTimeRange> vnBookings = bookings.stream()
                .map(b -> new BookingTimeRange(utcToVn(b.getStartTime()), utcToVn(b.getEndTime())))
                .toList();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = month.atDay(day);
            AvailabilityStatus status = calculateDayStatus(totalPorts, date, vnBookings);
            response.add(new CalendarStatusResponse(date, status));
        }

        return response;
    }

    private AvailabilityStatus calculateDayStatus(int totalPorts, LocalDate date, List<BookingTimeRange> monthBookings) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<BookingTimeRange> dayBookings = monthBookings.stream()
                .filter(b -> b.startTime().isBefore(dayEnd) && b.endTime().isAfter(dayStart))
                .toList();

        if (dayBookings.isEmpty()) {
            return AvailabilityStatus.AVAILABLE;
        }

        List<AvailableSlotResponse> slots = calculateSlots(totalPorts, date, 1.0, dayBookings);
        boolean hasAvailableSlot = slots.stream().anyMatch(slot -> slot.getAvailablePorts() > 0);
        return hasAvailableSlot ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.FULL;
    }

    @Override
    public List<AvailableSlotResponse> getAvailableSlots(Long stationId, Long portId, LocalDate date,
            Double durationHour) {
        PortCounts portCounts = portCountProvider.getPortCounts(stationId);
        int totalPorts = portId != null ? 1 : portCounts.totalPorts();

        if (totalPorts == 0) {
            return Collections.emptyList();
        }

        // Convert VN local day range → UTC for DB query
        LocalDateTime vnStartOfDay = date.atStartOfDay();
        LocalDateTime vnEndOfDay = date.plusDays(1).atStartOfDay();
        LocalDateTime utcStart = vnToUtc(vnStartOfDay);
        LocalDateTime utcEnd = vnToUtc(vnEndOfDay);

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED,
                BookingStatus.IN_PROGRESS);
        List<Booking> bookings = bookingRepository.findOverlappingBookings(stationId,
                activeStatuses, utcStart, utcEnd);

        if (portId != null) {
            bookings = bookings.stream()
                    .filter(b -> b.getPortId().equals(portId))
                    .toList();
        }

        // Convert booking times UTC → VN for slot comparison
        List<BookingTimeRange> vnBookings = bookings.stream()
                .map(b -> new BookingTimeRange(utcToVn(b.getStartTime()), utcToVn(b.getEndTime())))
                .toList();

        List<AvailableSlotResponse> slots = calculateSlots(totalPorts, date, durationHour, vnBookings);

        // Apply station opening hours constraint if configured
        Optional<StationResponse> stationOpt = stationService.findById(stationId);
        if (stationOpt.isPresent()) {
            StationResponse station = stationOpt.get();
            if (station.openingHours() != null && !station.openingHours().isEmpty()) {
                java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
                Optional<StationOpeningHoursResponse> hoursOpt = station.openingHours().stream()
                        .filter(h -> h.dayOfWeek() == dayOfWeek)
                        .findFirst();

                if (hoursOpt.isPresent()) {
                    StationOpeningHoursResponse hours = hoursOpt.get();
                    if (Boolean.FALSE.equals(hours.isOpen())) {
                        // Closed on this day: return empty list
                        return Collections.emptyList();
                    } else {
                        LocalTime open = hours.openTime();
                        LocalTime close = hours.closeTime();
                        if (open != null && close != null) {
                            return slots.stream()
                                    .filter(s -> isSlotWithinOpeningHours(s.getStartTime(), s.getEndTime(), open,
                                            close))
                                    .toList();
                        }
                    }
                }
            }
        }

        return slots;
    }

    private boolean isSlotWithinOpeningHours(LocalTime start, LocalTime end, LocalTime open, LocalTime close) {
        if (open.equals(LocalTime.of(0, 0)) && close.equals(LocalTime.of(0, 0))) {
            return true;
        }

        if (start.isBefore(open)) {
            return false;
        }

        if (end.equals(LocalTime.of(0, 0))) {
            return close.equals(LocalTime.of(0, 0));
        }

        if (close.equals(LocalTime.of(0, 0))) {
            return true;
        }

        return !end.isAfter(close);
    }

    private List<AvailableSlotResponse> calculateSlots(int totalPorts, LocalDate date, Double durationHour,
            List<BookingTimeRange> bookings) {
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
                for (BookingTimeRange booking : bookings) {
                    if (booking.startTime().isBefore(intervalEnd) && booking.endTime().isAfter(intervalStart)) {
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
