package com.project.evgo.booking.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.request.CheckAvailabilityRequest;
import com.project.evgo.booking.request.CreateBookingRequest;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.booking.response.BookingStatsResponse;
import com.project.evgo.booking.response.MonthlyChartEntry;
import com.project.evgo.booking.response.OwnerBookingSummaryResponse;
import com.project.evgo.payment.response.InvoiceStatsResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.user.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter = 
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setMessageConverters(converter)
                .build();

        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void testGetById_Success() throws Exception {
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(1L);
        mockResponse.setStationId(10L);

        Mockito.when(bookingService.findById(1L)).thenReturn(Optional.of(mockResponse));

        mockMvc.perform(get("/api/v1/bookings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stationId").value(10));
    }

    @Test
    void testGetByUserId_Success() throws Exception {
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(1L);
        mockResponse.setUserId(5L);

        Mockito.when(bookingService.findByUserId(5L)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/v1/bookings/user/5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void testGetByPortId_Success() throws Exception {
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(1L);
        mockResponse.setPortId(20L);

        Mockito.when(bookingService.findByPortId(20L)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/v1/bookings/port/20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void testGetMyBookings_Success() throws Exception {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(1L);
        Mockito.when(bookingService.findByUserId(10L)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/v1/bookings/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void testGetOwnerBookings_Success() throws Exception {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        PageResponse<OwnerBookingSummaryResponse> mockPage = new PageResponse<>(java.util.Collections.emptyList(), 0, 10, 0, 0, true, true);
        Mockito.when(bookingService.getOwnerBookings(eq(10L), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/bookings/owner")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOwnerStats_Success() throws Exception {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        BookingStatsResponse stats = new BookingStatsResponse();
        Mockito.when(bookingService.getOwnerStats(10L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/bookings/owner/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOwnerInvoiceStats_Success() throws Exception {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        InvoiceStatsResponse stats = new InvoiceStatsResponse();
        Mockito.when(bookingService.getOwnerInvoiceStats(10L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/bookings/owner/invoice-stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOwnerMonthlyChart_Success() throws Exception {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        Mockito.when(bookingService.getOwnerMonthlyChart(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bookings/owner/monthly-chart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckAvailability_Success() throws Exception {
        CheckAvailabilityRequest req = new CheckAvailabilityRequest();
        req.setStationId(1L);
        req.setChargerId(2L);
        req.setPortId(3L);
        req.setStartTime(LocalDateTime.now().plusHours(1));
        req.setEndTime(LocalDateTime.now().plusHours(2));

        Mockito.doNothing().when(bookingService).checkAvailability(any());

        mockMvc.perform(post("/api/v1/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Available"));
    }

    @Test
    void testCreateBooking_Success() throws Exception {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setStationId(1L);
        req.setChargerId(2L);
        req.setPortId(3L);
        req.setVehicleId(4L);
        req.setStartTime(LocalDateTime.now().plusHours(1));
        req.setEndTime(LocalDateTime.now().plusHours(2));

        BookingResponse res = new BookingResponse();
        res.setId(100L);
        Mockito.when(bookingService.createBooking(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    void testGetBookingsByStatus_Success() throws Exception {
        PageResponse<BookingResponse> mockPage = new PageResponse<>(java.util.Collections.emptyList(), 0, 10, 0, 0, true, true);
        Mockito.when(bookingService.getBookingsByStatus(eq("PENDING"), eq(0), eq(10))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/bookings?status=PENDING&page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelBooking_Success() throws Exception {
        Mockito.doNothing().when(bookingService).cancelBooking(100L);

        mockMvc.perform(post("/api/v1/bookings/100/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking cancelled successfully"));
    }
}
