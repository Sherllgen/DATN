package com.project.evgo.booking;

import com.project.evgo.booking.internal.Booking;
import com.project.evgo.booking.internal.BookingDtoConverter;
import com.project.evgo.booking.internal.BookingRepository;
import com.project.evgo.booking.internal.BookingServiceImpl;
import com.project.evgo.booking.request.CheckAvailabilityRequest;
import com.project.evgo.booking.request.CreateBookingRequest;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.station.StationService;
import com.project.evgo.user.security.SecurityUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.booking.response.BookingStatsResponse;
import com.project.evgo.booking.response.OwnerBookingSummaryResponse;
import com.project.evgo.booking.response.MonthlyChartEntry;
import com.project.evgo.payment.response.InvoiceStatsResponse;
import com.project.evgo.station.response.PriceSettingResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

        @Mock
        private BookingRepository bookingRepository;

        @Mock
        private BookingDtoConverter converter;

        @Mock
        private com.project.evgo.payment.InvoiceService invoiceService;

        @Mock
        private PriceSettingService priceSettingService;

        @Mock
        private StringRedisTemplate redisTemplate;

        @Mock
        private ValueOperations<String, String> valueOperations;

        @Mock
        private StationService stationService;

        @InjectMocks
        private BookingServiceImpl bookingService;

        private MockedStatic<SecurityUtil> mockedSecurityUtil;

        @BeforeEach
        void setUp() {
                mockedSecurityUtil = mockStatic(SecurityUtil.class);
                mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
        }

        @AfterEach
        void tearDown() {
                mockedSecurityUtil.close();
        }

    @Test
    @DisplayName("checkAvailability_AllBlocksFree_ReturnsSuccess")
    void checkAvailability_AllBlocksFree_ReturnsSuccess() {
        // Given - 1 hour booking, should generate exactly 2x 30-min intervals
        CheckAvailabilityRequest req = new CheckAvailabilityRequest(1L, 1L, 100L,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(bookingRepository.existsByPortIdAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
                anyLong(), any(), any(), any())).thenReturn(false);



                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                when(valueOperations.setIfAbsent(anyString(), anyString(), eq(13L), eq(TimeUnit.MINUTES)))
                                .thenReturn(true);

                // When
                bookingService.checkAvailability(req);

                // Then
                verify(valueOperations, org.mockito.Mockito.times(2)).setIfAbsent(anyString(), anyString(), eq(13L),
                                eq(TimeUnit.MINUTES));
        }
    @Test
    @DisplayName("checkAvailability_LockedPort_ThrowsException")
    void checkAvailability_LockedPort_ThrowsException() {
        // Given
        CheckAvailabilityRequest req = new CheckAvailabilityRequest(1L, 1L, 100L,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(bookingRepository.existsByPortIdAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
                anyLong(), any(), any(), any())).thenReturn(false);


                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                when(valueOperations.setIfAbsent(anyString(), anyString(), eq(13L), eq(TimeUnit.MINUTES)))
                                .thenReturn(false);

                // When / Then
                assertThatThrownBy(() -> bookingService.checkAvailability(req))
                                .isInstanceOf(AppException.class)
                                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKING_SLOT_UNAVAILABLE);
        }

        @ParameterizedTest
        @CsvSource({
            "UPCOMING, CONFIRMED",
            "CANCELED, CANCELLED",
            "INVALID_STATUS, PENDING"
        })
        @DisplayName("getBookingsByStatus_ReturnsPaginatedList")
        void getBookingsByStatus_ReturnsPaginatedList(String inputStatus, BookingStatus expectedMappedStatus) {
                Booking booking = new Booking();
                booking.setId(1L);
                booking.setStatus(expectedMappedStatus);
                
                BookingResponse bookingResponse = BookingResponse.builder().id(1L).portId(100L).portNumber(2)
                                        .status(expectedMappedStatus)
                                        .build();
                when(converter.toResponse(any(Booking.class))).thenReturn(bookingResponse);

                Page<Booking> page = new PageImpl<>(List.of(booking));
                when(bookingRepository.findByStatus(eq(expectedMappedStatus), any(PageRequest.class)))
                                .thenReturn(page);

                PageResponse<BookingResponse> res = bookingService.getBookingsByStatus(inputStatus, 0, 10);

                assertThat(res).isNotNull();
                assertThat(res.content()).hasSize(1);
        }

        @Test
        @DisplayName("cancelBooking_ValidCondition_CancelsBooking")
        void cancelBooking_ValidCondition_CancelsBooking() {
                Booking booking = new Booking();
                booking.setId(1L);
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setStartTime(LocalDateTime.now().plusHours(3)); // > 2 hours ahead

                when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

                bookingService.cancelBooking(1L);

                verify(bookingRepository).save(any(Booking.class));
                assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCancelBookingScenarios")
        @DisplayName("cancelBooking_InvalidConditions_ThrowsException")
        void cancelBooking_InvalidConditions_ThrowsException(BookingStatus status, int hoursAhead, ErrorCode expectedError) {
                Booking booking = new Booking();
                booking.setId(1L);
                booking.setStatus(status);
                booking.setStartTime(LocalDateTime.now().plusHours(hoursAhead));

                when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

                assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                                .isInstanceOf(AppException.class)
                                .hasFieldOrPropertyWithValue("errorCode", expectedError);
        }

        private static Stream<Arguments> provideInvalidCancelBookingScenarios() {
                return Stream.of(
                    Arguments.of(BookingStatus.CONFIRMED, 1, ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED), // Too close
                    Arguments.of(BookingStatus.CANCELLED, 3, ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED), // Already canceled
                    Arguments.of(BookingStatus.IN_PROGRESS, 3, ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED) // Invalid status
                );
        }

        @Test
        @DisplayName("createBooking_WithOverlappingBlock_ThrowsAppException")
        void createBooking_WithOverlappingBlock_ThrowsAppException() {
                CreateBookingRequest req = new CreateBookingRequest(1L, 1L, 1L, 1L,
                                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                when(valueOperations.get(anyString())).thenReturn(null); // Simulates missing lock validation

                assertThatThrownBy(() -> bookingService.createBooking(req))
                                .isInstanceOf(AppException.class)
                                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKING_SLOT_UNAVAILABLE);
        }

        @Test
        @DisplayName("getOwnerBookings_WithStations_ReturnsBookings")
        void getOwnerBookings_WithStations_ReturnsBookings() {
                // Given
                Long ownerId = 1L;
                List<Long> stationIds = List.of(100L, 101L);
                when(stationService.getStationIdsByOwnerId(ownerId)).thenReturn(stationIds);

                Booking booking = new Booking();
                booking.setId(1L);
                Page<Booking> page = new PageImpl<>(List.of(booking));
                when(bookingRepository.findByStationIdIn(eq(stationIds), any(PageRequest.class))).thenReturn(page);

                when(converter.toOwnerSummaryListBulk(any()))
                                .thenReturn(List.of(OwnerBookingSummaryResponse.builder().id(1L).build()));

                // When
                PageResponse<OwnerBookingSummaryResponse> result = bookingService.getOwnerBookings(ownerId,
                                PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.content()).hasSize(1);
                verify(stationService).getStationIdsByOwnerId(ownerId);
                verify(bookingRepository).findByStationIdIn(eq(stationIds), any(PageRequest.class));
        }

        @Test
        @DisplayName("getOwnerBookings_NoStations_ReturnsEmptyPage")
        void getOwnerBookings_NoStations_ReturnsEmptyPage() {
                // Given
                Long ownerId = 1L;
                when(stationService.getStationIdsByOwnerId(ownerId)).thenReturn(List.of());

                // When
                PageResponse<OwnerBookingSummaryResponse> result = bookingService.getOwnerBookings(ownerId,
                                PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.content()).isEmpty();
                verify(stationService).getStationIdsByOwnerId(ownerId);
                verify(bookingRepository, org.mockito.Mockito.never()).findByStationIdIn(any(), any());
        }

        @Test
        @DisplayName("getOwnerStats_WithStations_ReturnsStats")
        void getOwnerStats_WithStations_ReturnsStats() {
                // Given
                Long ownerId = 1L;
                List<Long> stationIds = List.of(100L, 101L);
                when(stationService.getStationIdsByOwnerId(ownerId)).thenReturn(stationIds);

                List<BookingStatus> activeStatuses = List.of(BookingStatus.COMPLETED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);

                when(bookingRepository.countByStationIdInAndStatusIn(stationIds, activeStatuses))
                                .thenReturn(100L);

                when(bookingRepository.countDistinctUserIdByStationIdInAndStatusIn(stationIds, activeStatuses))
                                .thenReturn(80L);

                // When
                BookingStatsResponse result = bookingService.getOwnerStats(ownerId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getTotalBookings()).isEqualTo(100L);
                assertThat(result.getTotalCustomers()).isEqualTo(80L);
                verify(stationService).getStationIdsByOwnerId(ownerId);
        }

        @Test
        @DisplayName("getOwnerStats_NoStations_ReturnsZeros")
        void getOwnerStats_NoStations_ReturnsZeros() {
                // Given
                Long ownerId = 1L;
                when(stationService.getStationIdsByOwnerId(ownerId)).thenReturn(List.of());

                // When
                BookingStatsResponse result = bookingService.getOwnerStats(ownerId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getTotalBookings()).isEqualTo(0L);
                assertThat(result.getTotalCustomers()).isEqualTo(0L);
                verify(stationService).getStationIdsByOwnerId(ownerId);
                verify(bookingRepository, org.mockito.Mockito.never()).countByStationIdInAndStatusIn(any(), any());
        }

        @Test
        @DisplayName("getOwnerInvoiceStats_WithStations_ReturnsInvoiceStats")
        void getOwnerInvoiceStats_WithStations_ReturnsInvoiceStats() {
                // Given
                Long ownerId = 1L;
                List<Long> stationIds = List.of(100L, 101L);
                List<Long> bookingIds = List.of(10L, 11L, 12L);

                when(stationService.getStationIdsByOwnerId(ownerId)).thenReturn(stationIds);
                when(bookingRepository.findIdsByStationIdIn(stationIds)).thenReturn(bookingIds);

                InvoiceStatsResponse mockResponse = InvoiceStatsResponse.builder()
                                .totalRevenue(java.math.BigDecimal.valueOf(500000))
                                .revenueGrowth(0.0)
                                .build();

                when(invoiceService.getStatsByBookingIds(bookingIds)).thenReturn(mockResponse);

                // When
                InvoiceStatsResponse result = bookingService.getOwnerInvoiceStats(ownerId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getTotalRevenue()).isEqualTo(java.math.BigDecimal.valueOf(500000));
                verify(stationService).getStationIdsByOwnerId(ownerId);
                verify(bookingRepository).findIdsByStationIdIn(stationIds);
                verify(invoiceService).getStatsByBookingIds(bookingIds);
        }

    @Test
    @DisplayName("startBookingSession_ConfirmedBooking_TransitionsToInProgress")
    void startBookingSession_ConfirmedBooking_TransitionsToInProgress() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.startBookingSession(1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("startBookingSession_NonConfirmedBooking_ThrowsException")
    void startBookingSession_NonConfirmedBooking_ThrowsException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.IN_PROGRESS); // Already in progress

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.startBookingSession(1L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("findById_ValidId_ReturnsBooking")
    void findById_ValidId_ReturnsBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        BookingResponse response = BookingResponse.builder().id(1L).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(converter.toResponse(any(Optional.class))).thenReturn(Optional.of(response));

        Optional<BookingResponse> result = bookingService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByUserId_ValidUserId_ReturnsBookings")
    void findByUserId_ValidUserId_ReturnsBookings() {
        Booking booking = new Booking();
        booking.setId(1L);
        BookingResponse response = BookingResponse.builder().id(1L).build();
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));
        when(converter.toResponseList(any())).thenReturn(List.of(response));

        List<BookingResponse> result = bookingService.findByUserId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByPortId_ValidPortId_ReturnsBookings")
    void findByPortId_ValidPortId_ReturnsBookings() {
        Booking booking = new Booking();
        booking.setId(1L);
        BookingResponse response = BookingResponse.builder().id(1L).build();
        when(bookingRepository.findByPortId(1L)).thenReturn(List.of(booking));
        when(converter.toResponseList(any())).thenReturn(List.of(response));

        List<BookingResponse> result = bookingService.findByPortId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("checkAvailability_HasOverlapDB_ThrowsException")
    void checkAvailability_HasOverlapDB_ThrowsException() {
        CheckAvailabilityRequest req = new CheckAvailabilityRequest(1L, 1L, 100L,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(bookingRepository.existsByPortIdAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
                anyLong(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.checkAvailability(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKING_SLOT_UNAVAILABLE);
    }

    @Test
    @DisplayName("createBooking_Success_ReturnsBooking")
    void createBooking_Success_ReturnsBooking() {
        CreateBookingRequest req = new CreateBookingRequest(1L, 1L, 1L, 1L,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("1"); // Match current userId
        
        PriceSettingResponse priceResponse = PriceSettingResponse.builder()
                .bookingFee(java.math.BigDecimal.valueOf(100))
                .build();
        when(priceSettingService.getActivePriceSetting(1L)).thenReturn(priceResponse);
        
        Booking saved = new Booking();
        saved.setId(10L);
        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);
        
        BookingResponse res = BookingResponse.builder().id(10L).build();
        when(converter.toResponse(any(Booking.class))).thenReturn(res);

        BookingResponse result = bookingService.createBooking(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(invoiceService).createInvoice(any());
    }



    @Test
    @DisplayName("getOwnerMonthlyChart_WithStations_ReturnsChart")
    void getOwnerMonthlyChart_WithStations_ReturnsChart() {
        Long ownerId = 1L;
        List<Long> stationIds = List.of(100L);
        when(stationService.getStationIdsByOwnerId(ownerId)).thenReturn(stationIds);
        when(bookingRepository.findIdsByStationIdIn(stationIds)).thenReturn(List.of(10L));
        
        Object[] row = {1, 5L}; // Month 1, 5 bookings
        when(bookingRepository.countMonthlyByStationIdsAndStatusAndYear(eq(stationIds), eq(BookingStatus.COMPLETED), org.mockito.ArgumentMatchers.anyInt()))
            .thenReturn(java.util.Collections.singletonList(row));
            
        when(invoiceService.getMonthlyRevenueByBookingIds(any(), org.mockito.ArgumentMatchers.anyInt()))
            .thenReturn(java.util.Map.of(1, java.math.BigDecimal.valueOf(500)));
            
        List<MonthlyChartEntry> result = bookingService.getOwnerMonthlyChart(ownerId);
        
        assertThat(result).hasSize(12);
        assertThat(result.get(0).getBookings()).isEqualTo(5L);
        assertThat(result.get(0).getRevenue()).isEqualTo(java.math.BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("getOwnerMonthlyChart_NoStations_ReturnsEmptyChart")
    void getOwnerMonthlyChart_NoStations_ReturnsEmptyChart() {
        Long ownerId = 1L;
        when(stationService.getStationIdsByOwnerId(ownerId)).thenReturn(List.of());
        
        List<MonthlyChartEntry> result = bookingService.getOwnerMonthlyChart(ownerId);
        
        assertThat(result).hasSize(12);
        assertThat(result.get(0).getBookings()).isEqualTo(0L);
        assertThat(result.get(0).getRevenue()).isEqualTo(java.math.BigDecimal.ZERO);
    }

    @Test
    @DisplayName("checkAvailability_WithStartTimeTooFarInPast_ThrowsAppException")
    void checkAvailability_WithStartTimeTooFarInPast_ThrowsAppException() {
        CheckAvailabilityRequest req = new CheckAvailabilityRequest(1L, 1L, 100L,
                LocalDateTime.now().minusMinutes(20), LocalDateTime.now().plusHours(1));

        assertThatThrownBy(() -> bookingService.checkAvailability(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("checkAvailability_WithEndTimeBeforeStartTime_ThrowsAppException")
    void checkAvailability_WithEndTimeBeforeStartTime_ThrowsAppException() {
        CheckAvailabilityRequest req = new CheckAvailabilityRequest(1L, 1L, 100L,
                LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(1));

        assertThatThrownBy(() -> bookingService.checkAvailability(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

}
