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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.booking.response.BookingResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingDtoConverter converter;

    @Mock
    private PriceSettingService priceSettingService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    @DisplayName("checkAvailability_AllBlocksFree_ReturnsSuccess")
    void checkAvailability_AllBlocksFree_ReturnsSuccess() {
        // Given - 1 hour booking, should generate exactly 2x 30-min intervals
        CheckAvailabilityRequest req = new CheckAvailabilityRequest(1L, 1L, 1, 
            LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 1L);
        
        when(bookingRepository.existsByStationIdAndPortNumberAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
            anyLong(), any(), any(), any(), any())).thenReturn(false);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), eq(8L), eq(TimeUnit.MINUTES)))
            .thenReturn(true);

        // When
        bookingService.checkAvailability(req);

        // Then
        verify(valueOperations, org.mockito.Mockito.times(2)).setIfAbsent(anyString(), anyString(), eq(8L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("checkAvailability_LockedPort_ThrowsException")
    void checkAvailability_LockedPort_ThrowsException() {
        // Given
        CheckAvailabilityRequest req = new CheckAvailabilityRequest(1L, 1L, 1, 
            LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 1L);
        
        when(bookingRepository.existsByStationIdAndPortNumberAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
            anyLong(), any(), any(), any(), any())).thenReturn(false);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), eq(8L), eq(TimeUnit.MINUTES)))
            .thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> bookingService.checkAvailability(req))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKING_SLOT_UNAVAILABLE);
    }



    @Test
    @DisplayName("getBookingsByStatus_ValidStatus_ReturnsPaginatedList")
    void getBookingsByStatus_ValidStatus_ReturnsPaginatedList() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPortNumber(2);
        
        Page<Booking> page = new PageImpl<>(List.of(booking));
        when(bookingRepository.findByStatus(eq(BookingStatus.CONFIRMED), any(PageRequest.class)))
            .thenReturn(page);
            
        BookingResponse bookingResponse = BookingResponse.builder().id(1L).portNumber(2).status(BookingStatus.CONFIRMED).build();
        when(converter.toResponse(any(Booking.class))).thenReturn(bookingResponse);
            
        PageResponse<BookingResponse> res = bookingService.getBookingsByStatus("UPCOMING", 0, 10);
        
        assertThat(res).isNotNull();
        assertThat(res.content()).hasSize(1);
        assertThat(res.content().get(0).getPortNumber()).isEqualTo(2);
        assertThat(res.content().get(0).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
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

    @Test
    @DisplayName("cancelBooking_TooCloseToStartTime_ThrowsException")
    void cancelBooking_TooCloseToStartTime_ThrowsException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setStartTime(LocalDateTime.now().plusHours(1)); // < 2 hours ahead
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        
        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED);
    }

    @Test
    @DisplayName("createBooking_WithOverlappingBlock_ThrowsAppException")
    void createBooking_WithOverlappingBlock_ThrowsAppException() {
        CreateBookingRequest req = new CreateBookingRequest(1L, 1L, 1, 1L, 1L,
            LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // Simulates missing lock validation

        assertThatThrownBy(() -> bookingService.createBooking(req))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKING_SLOT_UNAVAILABLE);
    }
}
