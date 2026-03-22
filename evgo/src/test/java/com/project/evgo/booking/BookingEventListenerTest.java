package com.project.evgo.booking;

import com.project.evgo.booking.internal.Booking;
import com.project.evgo.booking.internal.BookingEventListener;
import com.project.evgo.booking.internal.BookingRepository;
import com.project.evgo.payment.PaymentSuccessEvent;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingEventListenerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        booking.setPortNumber(1);
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
        verify(redisTemplate, times(2)).delete(anyString());
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
        booking.setPortNumber(2);
        booking.setUserId(42L);
        booking.setStatus(BookingStatus.PENDING);
        // Close to start (5 mins out)
        booking.setStartTime(LocalDateTime.now().plusMinutes(5));
        booking.setEndTime(LocalDateTime.now().plusHours(1));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When
        eventListener.onPaymentSuccess(event);

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        
        ArgumentCaptor<SendReserveNowCommandEvent> captor = ArgumentCaptor.forClass(SendReserveNowCommandEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        
        SendReserveNowCommandEvent publishedEvent = captor.getValue();
        assertThat(publishedEvent.chargePointId()).isEqualTo("10");
        assertThat(publishedEvent.connectorId()).isEqualTo(2);
        assertThat(publishedEvent.idTag()).isEqualTo("user-42");
    }
}
