package com.project.evgo.booking.internal;

import com.project.evgo.booking.SendPushNotificationEvent;
import com.project.evgo.booking.SendRemoteStopCommandEvent;
import com.project.evgo.booking.SendReserveNowCommandEvent;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.enums.PortStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler that bridges software bookings to physical hardware via OCPP events.
 * Runs 4 jobs every minute.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final ChargerService chargerService;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "evgo:booking:lock:*";

    // ============================================================
    // Job 1: Clean up stuck Redis lock keys older than 15 minutes
    // ============================================================
    @Scheduled(fixedRate = 60000)
    public void cleanStuckRedisKeys() {
        Set<String> keys = redisTemplate.keys(LOCK_PREFIX);
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
            // Keys with no TTL (ttl == -1) are stuck — original TTL is 8 min
            if (ttl != null && ttl == -1) {
                redisTemplate.delete(key);
                log.info("Cleaned up stuck Redis key: {}", key);
            }
        }
    }

    // ============================================================
    // Main Job: Unified periodic check for all booking-related actions
    // Runs every 60 seconds. Consolidates 3 previous jobs into 1 query.
    // ============================================================
    @Scheduled(fixedRate = 60000)
    public void processBookings() {
        LocalDateTime now = LocalDateTime.now();
        
        // Window T-10 mins (9 to 10) for Pre-arrival lock and Hard cutoff
        LocalDateTime startWindowFrom = now.plusMinutes(9);
        LocalDateTime startWindowTo = now.plusMinutes(10);
        
        // Window T-15 mins (14 to 15) for Soft warning
        LocalDateTime endWindowFrom = now.plusMinutes(14);
        LocalDateTime endWindowTo = now.plusMinutes(15);

        List<Booking> bookings = bookingRepository.findBookingsNeedingAction(
                List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS),
                startWindowFrom, startWindowTo,
                endWindowFrom, endWindowTo
        );

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                // Check if it's the start window for hardware lock
                if (booking.getStartTime().isAfter(startWindowFrom) && booking.getStartTime().isBefore(startWindowTo)) {
                    handlePreArrivalLock(booking);
                }
            } else if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
                // Check if it's the soft warning window (T-15)
                if (booking.getEndTime().isAfter(endWindowFrom) && booking.getEndTime().isBefore(endWindowTo)) {
                    handleSoftWarning(booking);
                }
                // Check if it's the hard cutoff window (T-10)
                if (booking.getEndTime().isAfter(startWindowFrom) && booking.getEndTime().isBefore(startWindowTo)) {
                    handleHardCutoff(booking);
                }
            }
        }
    }

    private void handlePreArrivalLock(Booking booking) {
        String chargePointId = String.valueOf(booking.getChargerId());
        PortStatus portStatus = resolvePortStatus(booking.getChargerId(), booking.getPortNumber());
        
        if (portStatus == PortStatus.CHARGING) {
            log.warn("Port {}:{} is still charging (overstay). Sending RemoteStop.",
                    booking.getChargerId(), booking.getPortNumber());
            eventPublisher.publishEvent(new SendRemoteStopCommandEvent(chargePointId, 0, "overstay"));
        }

        String idTag = "user-" + booking.getUserId();
        eventPublisher.publishEvent(new SendReserveNowCommandEvent(
                chargePointId, booking.getPortNumber(), idTag,
                booking.getEndTime(), booking.getId().intValue()));

        log.info("Pre-arrival lock dispatched: booking={}, chargePointId={}, port={}",
                booking.getId(), chargePointId, booking.getPortNumber());
    }

    private void handleSoftWarning(Booking booking) {
        eventPublisher.publishEvent(new SendPushNotificationEvent(
                booking.getUserId(),
                "Charging Session Ending Soon",
                "Your charging session will end in 15 minutes. Please prepare to unplug."));
        log.info("Soft warning sent: booking={}, userId={}", booking.getId(), booking.getUserId());
    }

    private void handleHardCutoff(Booking booking) {
        String chargePointId = String.valueOf(booking.getChargerId());
        eventPublisher.publishEvent(new SendRemoteStopCommandEvent(chargePointId, 0, "hard-cutoff"));
        
        eventPublisher.publishEvent(new SendPushNotificationEvent(
                booking.getUserId(),
                "Đã ngắt sạc an toàn",
                "Đã ngắt sạc. Bạn có 10 phút để dắt xe ra khỏi ô đỗ (trước giờ kết thúc) để tránh phí phạt chiếm chỗ."));
        
        log.info("Hard cut-off dispatched: booking={}, chargePointId={}, port={}",
                booking.getId(), chargePointId, booking.getPortNumber());
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * Resolves the current port status for a given charger and port number.
     */
    private PortStatus resolvePortStatus(Long chargerId, Integer portNumber) {
        return chargerService.findPortByChargerIdAndPortNumber(chargerId, portNumber)
                .map(PortResponse::getStatus)
                .orElse(PortStatus.UNAVAILABLE);
    }
}
