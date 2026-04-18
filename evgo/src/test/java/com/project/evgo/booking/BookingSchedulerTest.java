package com.project.evgo.booking;

import com.project.evgo.booking.internal.Booking;
import com.project.evgo.booking.internal.BookingRepository;
import com.project.evgo.booking.internal.BookingScheduler;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.enums.PortStatus;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ChargerService chargerService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private BookingScheduler bookingScheduler;

    @Test
    @DisplayName("Should delete Redis keys with no TTL (stuck) using optimized SCAN")
    @SuppressWarnings("unchecked")
    void cleanStuckRedisKeys_DeletesStuckKeys() {
        org.springframework.data.redis.connection.RedisConnection connection = mock(org.springframework.data.redis.connection.RedisConnection.class);
        org.springframework.data.redis.connection.RedisKeyCommands keyCommands = mock(org.springframework.data.redis.connection.RedisKeyCommands.class);
        org.springframework.data.redis.core.Cursor<byte[]> cursor = mock(org.springframework.data.redis.core.Cursor.class);

        when(connection.keyCommands()).thenReturn(keyCommands);
        when(keyCommands.scan(any(org.springframework.data.redis.core.ScanOptions.class))).thenReturn(cursor);

        byte[] stuckKey = "evgo:booking:lock:1:1:TIME".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(stuckKey);

        when(keyCommands.ttl(stuckKey)).thenReturn(-1L);

        when(redisTemplate.execute(any(org.springframework.data.redis.core.RedisCallback.class))).thenAnswer(invocation -> {
            org.springframework.data.redis.core.RedisCallback<Void> callback = invocation.getArgument(0);
            return callback.doInRedis(connection);
        });

        bookingScheduler.cleanStuckRedisKeys();

        verify(keyCommands).del(stuckKey);
    }

    @Test
    @DisplayName("processBookings: Should dispatch ReserveNow for upcoming CONFIRMED booking")
    void processBookings_UpcomingConfirmed_DispatchesReserveNow() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = buildBooking(1L, BookingStatus.CONFIRMED, 10L, 1, 42L,
                now.plusMinutes(9).plusSeconds(30), now.plusHours(1));

        when(bookingRepository.findBookingsNeedingAction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(booking));
        
        when(chargerService.findPortByChargerIdAndPortNumber(10L, 1))
                .thenReturn(Optional.of(PortResponse.builder().portNumber(1).status(PortStatus.AVAILABLE).build()));

        bookingScheduler.processBookings();

        ArgumentCaptor<SendReserveNowCommandEvent> captor = ArgumentCaptor.forClass(SendReserveNowCommandEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().chargePointId()).isEqualTo("10");
        assertThat(captor.getValue().connectorId()).isEqualTo(1);
    }

    @Test
    @DisplayName("processBookings: Should dispatch Push Notification for IN_PROGRESS booking ending soon")
    void processBookings_InProgressEndingSoon_DispatchesPush() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = buildBooking(2L, BookingStatus.IN_PROGRESS, 10L, 1, 42L,
                now.minusMinutes(30), now.plusMinutes(14).plusSeconds(30));

        when(bookingRepository.findBookingsNeedingAction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingScheduler.processBookings();

        ArgumentCaptor<SendPushNotificationEvent> captor = ArgumentCaptor.forClass(SendPushNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().title()).contains("Ending Soon");
    }

    @Test
    @DisplayName("processBookings: Should dispatch RemoteStop (Hard Cutoff) for IN_PROGRESS booking at T-10")
    void processBookings_InProgressAtT10_DispatchesRemoteStop() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = buildBooking(3L, BookingStatus.IN_PROGRESS, 10L, 1, 42L,
                now.minusMinutes(50), now.plusMinutes(9).plusSeconds(30));

        when(bookingRepository.findBookingsNeedingAction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingScheduler.processBookings();

        ArgumentCaptor<SendRemoteStopCommandEvent> captor = ArgumentCaptor.forClass(SendRemoteStopCommandEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().reason()).isEqualTo("hard-cutoff");
    }

    @Test
    @DisplayName("cleanupStalePendingBookings: Should cancel PENDING bookings older than 12 mins")
    void cleanupStalePendingBookings_OldPending_CancelsThem() {
        Booking stale = new Booking();
        stale.setId(100L);
        stale.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING), any()))
                .thenReturn(List.of(stale));

        bookingScheduler.cleanupStalePendingBookings();

        assertThat(stale.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(stale);
    }

    private Booking buildBooking(Long id, BookingStatus status, Long chargerId, Integer portNumber,
                                  Long userId, LocalDateTime start, LocalDateTime end) {
        Booking b = new Booking();
        b.setId(id);
        b.setStatus(status);
        b.setChargerId(chargerId);
        b.setPortNumber(portNumber);
        b.setUserId(userId);
        b.setStartTime(start);
        b.setEndTime(end);
        return b;
    }
}
