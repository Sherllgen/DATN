package com.project.evgo.booking;

import com.project.evgo.booking.internal.Booking;
import com.project.evgo.booking.internal.BookingEventListener;
import com.project.evgo.booking.internal.BookingRepository;
import com.project.evgo.payment.PaymentSuccessEvent;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.events.ChargingSessionCompletedEvent;
import com.project.evgo.sharedkernel.events.SendReserveNowCommandEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingEventListenerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private com.project.evgo.charger.ChargerService chargerService;

    @InjectMocks
    private BookingEventListener eventListener;

    @Test
    @DisplayName("onPaymentSuccess_UpdatesBookingAndDeletesLock")
    void onPaymentSuccess_UpdatesBookingAndDeletesLock() {
        // Given
        Long invoiceId = 99L;
        PaymentSuccessEvent event = new PaymentSuccessEvent(invoiceId, "appTrans123", "zpTrans456", 1L, null);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStationId(10L);
        booking.setPortId(1L);
        booking.setStatus(BookingStatus.PENDING);
        // Far in the future to not trigger immediate ReserveNow
        booking.setStartTime(LocalDateTime.now().plusHours(5));
        booking.setEndTime(LocalDateTime.now().plusHours(6));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When
        eventListener.onPaymentSuccess(event);

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
        verify(redisTemplate).delete(org.mockito.ArgumentMatchers.<java.util.Collection<String>>any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("onPaymentSuccess_NearStart_DispatchesImmediateReserveNow")
    void onPaymentSuccess_NearStart_DispatchesImmediateReserveNow() {
        // Given
        PaymentSuccessEvent event = new PaymentSuccessEvent(99L, "appTrans123", "zpTrans456", 1L, null);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setChargerId(10L);
        booking.setPortId(2L);
        booking.setUserId(42L);
        booking.setStatus(BookingStatus.PENDING);
        // Close to start (5 mins out)
        booking.setStartTime(LocalDateTime.now().plusMinutes(5));
        booking.setEndTime(LocalDateTime.now().plusHours(1));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(chargerService.findPortById(2L)).thenReturn(Optional.of(new com.project.evgo.charger.response.PortResponse(2L, 2, com.project.evgo.sharedkernel.enums.PortStatus.AVAILABLE, 10L, null)));

        // When
        eventListener.onPaymentSuccess(event);

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        
        ArgumentCaptor<BookingConfirmedAndReadyForHardwareEvent> captor = ArgumentCaptor.forClass(BookingConfirmedAndReadyForHardwareEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        
        BookingConfirmedAndReadyForHardwareEvent publishedEvent = captor.getValue();
        assertThat(publishedEvent.bookingId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("onBookingConfirmedReadyForHardware_DispatchesSendReserveNowCommandEvent")
    void onBookingConfirmedReadyForHardware_DispatchesSendReserveNowCommandEvent() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setChargerId(10L);
        booking.setPortId(2L);
        booking.setUserId(42L);
        booking.setEndTime(LocalDateTime.now().plusHours(1));

        BookingConfirmedAndReadyForHardwareEvent event = new BookingConfirmedAndReadyForHardwareEvent(
                booking.getId(),
                booking.getChargerId(),
                2, // Hardcoded port number for the test event
                booking.getUserId(),
                booking.getEndTime()
        );

        eventListener.onBookingConfirmedReadyForHardware(event);

        ArgumentCaptor<SendReserveNowCommandEvent> captor = ArgumentCaptor.forClass(SendReserveNowCommandEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        
        SendReserveNowCommandEvent publishedEvent = captor.getValue();
        assertThat(publishedEvent.chargePointId()).isEqualTo("10");
        assertThat(publishedEvent.connectorId()).isEqualTo(2);
        assertThat(publishedEvent.idTag()).isEqualTo("user-42");
    }

    @Test
    @DisplayName("onChargingSessionCompleted_WithBooking_TransitionsToCompleted")
    void onChargingSessionCompleted_WithBooking_TransitionsToCompleted() {
        // Given - event carries bookingId=1L directly
        ChargingSessionCompletedEvent event = new ChargingSessionCompletedEvent(
                100L, 42L, 5L, BigDecimal.valueOf(15.5), "Normal", 1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.IN_PROGRESS);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When
        eventListener.onChargingSessionCompleted(event);

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("onChargingSessionCompleted_NoBookingLinked_DoesNothing")
    void onChargingSessionCompleted_NoBookingLinked_DoesNothing() {
        // Given - event has null bookingId
        ChargingSessionCompletedEvent event = new ChargingSessionCompletedEvent(
                100L, 42L, 5L, BigDecimal.valueOf(10.0), "Normal", null);

        // When
        eventListener.onChargingSessionCompleted(event);

        // Then
        verify(bookingRepository, never()).findById(any());
        verify(bookingRepository, never()).save(any());
    }
}
