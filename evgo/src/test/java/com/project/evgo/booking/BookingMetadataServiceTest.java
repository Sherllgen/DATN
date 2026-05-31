package com.project.evgo.booking;

import com.project.evgo.booking.internal.Booking;
import com.project.evgo.booking.internal.BookingMetadataServiceImpl;
import com.project.evgo.booking.internal.BookingRepository;
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
import java.util.Optional;
import java.time.DayOfWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMetadataServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PortCountProvider portCountProvider;

    @Mock
    private StationService stationService;

    @InjectMocks
    private BookingMetadataServiceImpl service;

    @Test
    @DisplayName("Should return valid durations from 1.0 to 12.0 in 0.5 increments")
    void getDurations_ReturnsExpectedList() {
        // When
        DurationConfigResponse response = service.getDurations();

        // Then
        assertThat(response.getDurations()).hasSize(23);
        assertThat(response.getDurations().get(0)).isEqualTo(1.0);
        assertThat(response.getDurations().get(1)).isEqualTo(1.5);
        assertThat(response.getDurations().get(22)).isEqualTo(12.0);
    }

    @Test
    @DisplayName("Should return all CLOSED when port count is zero")
    void getCalendarStatus_NoPorts_ReturnsAllClosed() {
        // Given
        Long stationId = 1L;
        YearMonth month = YearMonth.of(2023, 10);
        when(portCountProvider.getPortCounts(stationId)).thenReturn(new PortCounts(0, 0));

        // When
        List<CalendarStatusResponse> response = service.getCalendarStatus(stationId, month);

        // Then
        assertThat(response).hasSize(31);
        assertThat(response).allMatch(r -> r.getStatus() == AvailabilityStatus.CLOSED);
    }

    @Test
    @DisplayName("Should return all AVAILABLE when port count > 0 and no bookings")
    void getCalendarStatus_WithAvailablePorts_ReturnsAllAvailable() {
        // Given
        Long stationId = 1L;
        YearMonth month = YearMonth.of(2023, 10);
        when(portCountProvider.getPortCounts(stationId)).thenReturn(new PortCounts(5, 5));
        when(bookingRepository.findByStationIdAndStatusInAndStartTimeBetween(
                eq(stationId), any(), any(), any())).thenReturn(Collections.emptyList());

        // When
        List<CalendarStatusResponse> response = service.getCalendarStatus(stationId, month);

        // Then
        assertThat(response).hasSize(31);
        assertThat(response).allMatch(r -> r.getStatus() == AvailabilityStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should calculate available slots correctly with exist bookings")
    void getAvailableSlots_WithBookings_CalculatesCorrectly() {
        // Given
        Long stationId = 1L;
        LocalDate date = LocalDate.of(2023, 10, 15);
        Double duration = 1.0;
        
        when(portCountProvider.getPortCounts(stationId)).thenReturn(new PortCounts(2, 2));
        
        Booking booking1 = new Booking();
        booking1.setStartTime(LocalDateTime.of(date, LocalTime.of(10, 0)));
        booking1.setEndTime(LocalDateTime.of(date, LocalTime.of(11, 0)));
        booking1.setStatus(BookingStatus.CONFIRMED);
        
        Booking booking2 = new Booking();
        booking2.setStartTime(LocalDateTime.of(date, LocalTime.of(10, 30)));
        booking2.setEndTime(LocalDateTime.of(date, LocalTime.of(11, 30)));
        booking2.setStatus(BookingStatus.IN_PROGRESS);

        when(bookingRepository.findByStationIdAndStatusInAndStartTimeBetween(
                eq(stationId), any(), any(), any())).thenReturn(Arrays.asList(booking1, booking2));

        // When
        List<AvailableSlotResponse> response = service.getAvailableSlots(stationId, null, date, duration);

        // Then
        // For slot 10:00-11:00, b1 overlaps (takes 1 port), b2 overlaps (starts at 10:30, takes 1 port). 
        // Max concurrent overlap during 10:00-11:00 is 2. So availablePorts = 0.
        AvailableSlotResponse slot10_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(10, 0)))
                .findFirst().orElseThrow();
        assertThat(slot10_00.getAvailablePorts()).isEqualTo(0);

        // For slot 10:30-11:30, b1 overlaps, b2 overlaps. Max concurrent overlap is 2. availablePorts = 0.
        AvailableSlotResponse slot10_30 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(10, 30)))
                .findFirst().orElseThrow();
        assertThat(slot10_30.getAvailablePorts()).isEqualTo(0);

        // For slot 11:00-12:00, b1 ends at 11:00 (no overlap), b2 overlaps. availablePorts = 1.
        AvailableSlotResponse slot11_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(11, 0)))
                .findFirst().orElseThrow();
        assertThat(slot11_00.getAvailablePorts()).isEqualTo(1);
        
        // Slot 08:00-09:00 has no overlaps. availablePorts = 2.
        AvailableSlotResponse slot08_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(8, 0)))
                .findFirst().orElseThrow();
        assertThat(slot08_00.getAvailablePorts()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should calculate port-specific available slots correctly filtering other port bookings")
    void getAvailableSlots_WithPortIdAndBookings_CalculatesPortAvailabilityCorrectly() {
        // Given
        Long stationId = 1L;
        Long portId = 100L;
        LocalDate date = LocalDate.of(2023, 10, 15);
        Double duration = 1.0;
        
        when(portCountProvider.getPortCounts(stationId)).thenReturn(new PortCounts(2, 2));
        
        Booking booking1 = new Booking();
        booking1.setPortId(100L); // target port
        booking1.setStartTime(LocalDateTime.of(date, LocalTime.of(10, 0)));
        booking1.setEndTime(LocalDateTime.of(date, LocalTime.of(11, 0)));
        booking1.setStatus(BookingStatus.CONFIRMED);
        
        Booking booking2 = new Booking();
        booking2.setPortId(200L); // other port
        booking2.setStartTime(LocalDateTime.of(date, LocalTime.of(10, 30)));
        booking2.setEndTime(LocalDateTime.of(date, LocalTime.of(11, 30)));
        booking2.setStatus(BookingStatus.IN_PROGRESS);

        when(bookingRepository.findByStationIdAndStatusInAndStartTimeBetween(
                eq(stationId), any(), any(), any())).thenReturn(Arrays.asList(booking1, booking2));

        // When
        List<AvailableSlotResponse> response = service.getAvailableSlots(stationId, portId, date, duration);

        // Then
        // For slot 10:00-11:00, b1 overlaps (takes 1 port). b2 is ignored.
        // Max concurrent overlap is 1. Since total capacity is 1, availablePorts = 0.
        AvailableSlotResponse slot10_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(10, 0)))
                .findFirst().orElseThrow();
        assertThat(slot10_00.getAvailablePorts()).isEqualTo(0);

        // For slot 11:00-12:00, b1 ends at 11:00. b2 is ignored.
        // No overlap. capacity = 1, availablePorts = 1.
        AvailableSlotResponse slot11_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(11, 0)))
                .findFirst().orElseThrow();
        assertThat(slot11_00.getAvailablePorts()).isEqualTo(1);
        
        // Slot 08:00-09:00 has no overlaps. capacity = 1, availablePorts = 1.
        AvailableSlotResponse slot08_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(8, 0)))
                .findFirst().orElseThrow();
        assertThat(slot08_00.getAvailablePorts()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should filter slots outside opening hours")
    void getAvailableSlots_WithOpeningHours_FiltersSlotsOutsideOpeningHours() {
        // Given
        Long stationId = 1L;
        LocalDate date = LocalDate.of(2023, 10, 16); // Monday
        Double duration = 1.0;
        
        when(portCountProvider.getPortCounts(stationId)).thenReturn(new PortCounts(1, 1));
        when(bookingRepository.findByStationIdAndStatusInAndStartTimeBetween(
                eq(stationId), any(), any(), any())).thenReturn(Collections.emptyList());

        StationOpeningHoursResponse mondayHours = StationOpeningHoursResponse.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(20, 0))
                .isOpen(true)
                .build();

        StationResponse station = StationResponse.builder()
                .id(stationId)
                .openingHours(Arrays.asList(mondayHours))
                .build();

        when(stationService.findById(stationId)).thenReturn(Optional.of(station));

        // When
        List<AvailableSlotResponse> response = service.getAvailableSlots(stationId, null, date, duration);

        // Then
        // 07:30 - 08:30 is outside bounds (starts before 08:00) -> should be filtered out
        boolean hasSlot07_30 = response.stream()
                .anyMatch(s -> s.getStartTime().equals(LocalTime.of(7, 30)));
        assertThat(hasSlot07_30).isFalse();

        // 08:00 - 09:00 is within bounds
        AvailableSlotResponse slot08_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(8, 0)))
                .findFirst().orElseThrow();
        assertThat(slot08_00.getAvailablePorts()).isEqualTo(1);

        // 19:30 - 20:30 is outside bounds (ends after 20:00) -> should be filtered out
        boolean hasSlot19_30 = response.stream()
                .anyMatch(s -> s.getStartTime().equals(LocalTime.of(19, 30)));
        assertThat(hasSlot19_30).isFalse();

        // 19:00 - 20:00 is within bounds
        AvailableSlotResponse slot19_00 = response.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(19, 0)))
                .findFirst().orElseThrow();
        assertThat(slot19_00.getAvailablePorts()).isEqualTo(1);
    }
}
