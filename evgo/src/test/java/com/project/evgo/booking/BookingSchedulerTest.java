package com.project.evgo.booking;

import com.project.evgo.booking.internal.Booking;
import com.project.evgo.booking.internal.BookingRepository;
import com.project.evgo.booking.internal.BookingScheduler;
import com.project.evgo.sharedkernel.events.SendPushNotificationEvent;
import com.project.evgo.sharedkernel.events.SendRemoteStopCommandEvent;
import com.project.evgo.sharedkernel.events.SendReserveNowCommandEvent;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.ZaloPayService;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.enums.PortStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
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
    private BookingService bookingService;

    @Mock
    private ChargerService chargerService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private ZaloPayService zaloPayService;

    @InjectMocks
    private BookingScheduler bookingScheduler;

    @Test
    @DisplayName("Should delete Redis keys with no TTL (stuck) using optimized SCAN")
    @SuppressWarnings("unchecked")
    void cleanStuckRedisKeys_DeletesStuckKeys() {
        RedisConnection connection = mock(RedisConnection.class);
        RedisKeyCommands keyCommands = mock(RedisKeyCommands.class);
        Cursor<byte[]> cursor = mock(Cursor.class);

        when(connection.keyCommands()).thenReturn(keyCommands);
        when(keyCommands.scan(any(ScanOptions.class))).thenReturn(cursor);

        byte[] stuckKey = "evgo:booking:lock:100:TIME".getBytes(java.nio.charset.StandardCharsets.UTF_8);
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
        Booking booking = buildBooking(1L, BookingStatus.CONFIRMED, 10L, 100L, 42L,
                now.plusMinutes(9).plusSeconds(30), now.plusHours(1));

        when(bookingRepository.findBookingsNeedingAction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(booking));
        
        // resolvePortStatus and resolvePortNumber both call findPortById
        when(chargerService.findPortById(100L))
                .thenReturn(Optional.of(PortResponse.builder().id(100L).portNumber(1).status(PortStatus.AVAILABLE).build()));

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
        Booking booking = buildBooking(2L, BookingStatus.IN_PROGRESS, 10L, 100L, 42L,
                now.minusMinutes(30), now.plusMinutes(14).plusSeconds(30));

        when(bookingRepository.findBookingsNeedingAction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingScheduler.processBookings();

        ArgumentCaptor<SendPushNotificationEvent> captor = ArgumentCaptor.forClass(SendPushNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().title()).contains("Ending Soon");
    }

    @Test
    @DisplayName("processBookings: Should dispatch RemoteStop when next booking exists on same port")
    void processBookings_InProgressAtT10_WithNextBooking_DispatchesRemoteStop() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = buildBooking(3L, BookingStatus.IN_PROGRESS, 10L, 100L, 42L,
                now.minusMinutes(50), now.plusMinutes(9).plusSeconds(30));

        when(bookingRepository.findBookingsNeedingAction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(booking));

        // Another booking follows on the same port (now uses portId only)
        when(bookingService.hasUpcomingBookingOnPort(eq(100L), any()))
                .thenReturn(true);

        bookingScheduler.processBookings();

        ArgumentCaptor<SendRemoteStopCommandEvent> captor = ArgumentCaptor.forClass(SendRemoteStopCommandEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().reason()).isEqualTo("hard-cutoff");
    }

    @Test
    @DisplayName("processBookings: Should SKIP RemoteStop when no next booking exists on port")
    void processBookings_InProgressAtT10_NoNextBooking_SkipsCutoff() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = buildBooking(4L, BookingStatus.IN_PROGRESS, 10L, 100L, 42L,
                now.minusMinutes(50), now.plusMinutes(9).plusSeconds(30));

        when(bookingRepository.findBookingsNeedingAction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(booking));

        // No upcoming booking on this port (now uses portId only)
        when(bookingService.hasUpcomingBookingOnPort(eq(100L), any()))
                .thenReturn(false);

        bookingScheduler.processBookings();

        // RemoteStop should NOT be dispatched
        verify(eventPublisher, never()).publishEvent(any(SendRemoteStopCommandEvent.class));
        // No push notification either
        verify(eventPublisher, never()).publishEvent(any(SendPushNotificationEvent.class));
    }

    @Test
    @DisplayName("cleanupStalePendingBookings: Should cancel PENDING bookings older than 12 mins and cancel their invoices")
    void cleanupStalePendingBookings_OldPending_CancelsThem() {
        Booking stale = new Booking();
        stale.setId(100L);
        stale.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING), any()))
                .thenReturn(List.of(stale));

        bookingScheduler.cleanupStalePendingBookings();

        // B2: Booking status must be CANCELLED
        assertThat(stale.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(stale);
        // B2: Linked PENDING invoice must also be cancelled so user is not blocked
        verify(invoiceService).cancelInvoiceByBookingId(100L);
    }

    @Test
    @DisplayName("pollZaloPayPendingInvoices: Should call queryOrderStatus for each stale PENDING invoice")
    void pollZaloPayPendingInvoices_ShouldQueryGatewayForStaleInvoices() {
        InvoiceResponse staleInvoice = InvoiceResponse.builder()
                .id(55L)
                .status(InvoiceStatus.PENDING)
                .build();

        when(invoiceService.findPendingOlderThan(any())).thenReturn(List.of(staleInvoice));
        when(invoiceService.getLatestAppTransId(55L)).thenReturn("260518_abc12345");

        bookingScheduler.pollZaloPayPendingInvoices();

        verify(zaloPayService).queryOrderStatus("260518_abc12345");
    }

    @Test
    @DisplayName("pollZaloPayPendingInvoices: Should skip invoice with no transaction")
    void pollZaloPayPendingInvoices_NoTransaction_Skips() {
        InvoiceResponse staleInvoice = InvoiceResponse.builder()
                .id(99L)
                .status(InvoiceStatus.PENDING)
                .build();

        when(invoiceService.findPendingOlderThan(any())).thenReturn(List.of(staleInvoice));
        when(invoiceService.getLatestAppTransId(99L)).thenReturn(null);

        bookingScheduler.pollZaloPayPendingInvoices();

        verify(zaloPayService, never()).queryOrderStatus(any());
    }

    private Booking buildBooking(Long id, BookingStatus status, Long chargerId, Long portId,
                                  Long userId, LocalDateTime start, LocalDateTime end) {
        Booking b = new Booking();
        b.setId(id);
        b.setStatus(status);
        b.setChargerId(chargerId);
        b.setPortId(portId);
        b.setUserId(userId);
        b.setStartTime(start);
        b.setEndTime(end);
        return b;
    }
}
