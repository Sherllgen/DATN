package com.project.evgo.booking.internal;

import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.booking.response.OwnerBookingSummaryResponse;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.station.StationService;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.user.UserService;
import com.project.evgo.user.VehicleService;
import com.project.evgo.user.response.UserResponse;
import com.project.evgo.user.response.VehicleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingDtoConverterTest {

    @Mock
    private StationService stationService;

    @Mock
    private ChargerService chargerService;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingDtoConverter bookingDtoConverter;

    @Test
    void toResponse_ShouldConvertSuccessfully() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUserId(10L);
        booking.setStationId(100L);
        booking.setChargerId(1000L);
        booking.setVehicleId(200L);
        booking.setPortId(2000L);
        booking.setStartTime(LocalDateTime.now());
        booking.setEndTime(LocalDateTime.now().plusHours(1));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(new BigDecimal("50.0"));
        booking.setCreatedAt(LocalDateTime.now());

        StationResponse stationResponse = StationResponse.builder()
                .id(100L)
                .name("Test Station")
                .address("123 Test St")
                .build();
        when(stationService.findById(100L)).thenReturn(Optional.of(stationResponse));

        ChargerResponse charger = ChargerResponse.builder()
                .name("Fast Charger")
                .connectorType(ConnectorType.VINFAST_STD)
                .maxPower(50.0)
                .build();
        when(chargerService.findById(1000L)).thenReturn(Optional.of(charger));

        PortResponse port = PortResponse.builder()
                .portNumber(1)
                .build();
        when(chargerService.findPortById(2000L)).thenReturn(Optional.of(port));

        VehicleResponse vehicleResponse = new VehicleResponse();
        vehicleResponse.setBrand("VinFast");
        vehicleResponse.setModelName("VF8");
        when(vehicleService.getVehicleById(200L)).thenReturn(vehicleResponse);

        BookingResponse response = bookingDtoConverter.toResponse(booking);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Station", response.getStationName());
        assertEquals("123 Test St", response.getStationAddress());
        assertEquals("Fast Charger", response.getChargerName());
        assertEquals(ConnectorType.VINFAST_STD, response.getConnectorType());
        assertEquals(50.0, response.getMaxPower());
        assertEquals(1, response.getPortNumber());
        assertEquals("VinFast", response.getVehicleBrand());
        assertEquals("VF8", response.getVehicleModelName());
    }

    @Test
    void toResponse_ShouldHandleMissingDependenciesGracefully() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStationId(100L);
        booking.setChargerId(1000L);
        booking.setPortId(2000L);
        booking.setVehicleId(200L);

        when(stationService.findById(any())).thenReturn(Optional.empty());
        when(chargerService.findById(any())).thenReturn(Optional.empty());
        when(chargerService.findPortById(any())).thenReturn(Optional.empty());
        try {
            when(vehicleService.getVehicleById(any())).thenThrow(new RuntimeException("Vehicle API error"));
        } catch (Exception e) {}

        BookingResponse response = bookingDtoConverter.toResponse(booking);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertNull(response.getStationName());
        assertNull(response.getChargerName());
        assertNull(response.getPortNumber());
        assertNull(response.getVehicleBrand());
    }

    @Test
    void toResponseList_ShouldConvertMultipleBookings() {
        Booking b1 = new Booking();
        b1.setId(1L);
        Booking b2 = new Booking();
        b2.setId(2L);

        List<BookingResponse> responses = bookingDtoConverter.toResponseList(List.of(b1, b2));

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
    }

    @Test
    void toOwnerSummaryListBulk_ShouldConvertSuccessfully() {
        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setUserId(10L);
        b1.setStationId(100L);
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setTotalPrice(new BigDecimal("50.0"));
        b1.setCreatedAt(LocalDateTime.now());

        StationResponse stationResponse = StationResponse.builder()
                .id(100L)
                .name("Station A")
                .build();
        
        UserResponse userResponse = UserResponse.builder()
                .id(10L)
                .email("test@mail.com")
                .phoneNumber("0123456789")
                .fullName("John Doe")
                .build();

        when(stationService.findAllByIds(Set.of(100L))).thenReturn(List.of(stationResponse));
        when(userService.findAllByIds(Set.of(10L))).thenReturn(List.of(userResponse));

        List<OwnerBookingSummaryResponse> summaries = bookingDtoConverter.toOwnerSummaryListBulk(List.of(b1));

        assertEquals(1, summaries.size());
        OwnerBookingSummaryResponse summary = summaries.get(0);
        assertEquals(1L, summary.getId());
        assertEquals("Station A", summary.getStationName());
        assertEquals("John Doe", summary.getCustomerName());
    }

    @Test
    void toOwnerSummaryListBulk_ShouldHandleEmptyList() {
        List<OwnerBookingSummaryResponse> result = bookingDtoConverter.toOwnerSummaryListBulk(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void toOwnerSummaryListBulk_ShouldHandleNullList() {
        List<OwnerBookingSummaryResponse> result = bookingDtoConverter.toOwnerSummaryListBulk(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void toResponse_Optional_ShouldConvertSuccessfully() {
        Booking booking = new Booking();
        booking.setId(1L);
        Optional<BookingResponse> response = bookingDtoConverter.toResponse(Optional.of(booking));
        
        assertTrue(response.isPresent());
        assertEquals(1L, response.get().getId());
    }

    @Test
    void toResponse_Optional_ShouldHandleEmpty() {
        Optional<BookingResponse> response = bookingDtoConverter.toResponse(Optional.empty());
        assertTrue(response.isEmpty());
    }
}
