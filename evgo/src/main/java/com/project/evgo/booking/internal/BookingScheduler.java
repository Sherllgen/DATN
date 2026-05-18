package com.project.evgo.booking.internal;

import com.project.evgo.sharedkernel.events.SendPushNotificationEvent;
import com.project.evgo.sharedkernel.events.SendRemoteStopCommandEvent;
import com.project.evgo.sharedkernel.events.SendReserveNowCommandEvent;
import com.project.evgo.booking.BookingService;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.ZaloPayService;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.enums.PortStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;

/**
 * Scheduler that bridges software bookings to physical hardware via OCPP events.
 * Runs 4 jobs every minute.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final ChargerService chargerService;
    private final InvoiceService invoiceService;
    private final ZaloPayService zaloPayService;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "evgo:booking:lock:*";

    // ============================================================
    // Job 1: Clean up Redis keys that are stuck without a TTL
    // ============================================================
    @Scheduled(fixedRate = 60000)
    public void cleanStuckRedisKeys() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(LOCK_PREFIX)
                    .count(100)
                    .build();
            
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    byte[] rawKey = cursor.next();
                    
                    Long ttl = connection.keyCommands().ttl(rawKey);
                    
                    if (ttl != null && ttl == -1) {
                        connection.keyCommands().del(rawKey);
                        
                        String keyStr = new String(rawKey, StandardCharsets.UTF_8);
                        log.info("Cleaned up stuck Redis key: {}", keyStr);
                    }
                }
            } catch (Exception e) {
                log.error("Error scanning and cleaning redis keys", e);
            }
            
            return null;
        });
    }

    // ============================================================
    // Job 2: Cancel PENDING bookings older than 12 minutes
    // (8 min for Redis Lock + 4 min safety buffer for payment latency)
    // ============================================================
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupStalePendingBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(12);
        List<Booking> staleBookings = bookingRepository.findByStatusAndCreatedAtBefore(
                BookingStatus.PENDING, threshold);

        if (!staleBookings.isEmpty()) {
            for (Booking booking : staleBookings) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                //Cancel any linked PENDING invoice so the user is not blocked from future bookings.
                invoiceService.cancelInvoiceByBookingId(booking.getId());
                log.info("Cancelled stale PENDING booking {} and its linked invoice.", booking.getId());
            }
        }
    }

    // ============================================================
    // Main Job: Unified periodic check for all booking-related actions
    // Runs every 60 seconds. Consolidates 3 previous jobs into 1 query.
    // ============================================================
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processBookings() {
        LocalDateTime now = LocalDateTime.now();
        
        // Window T-10 mins (9 to 10) for Pre-arrival lock and Hard cutoff
        LocalDateTime startWindowFrom = now.plusMinutes(9);
        LocalDateTime startWindowTo = now.plusMinutes(10);
        
        // Window T-30 mins (29 to 30) for Booking Reminder
        LocalDateTime reminderWindowFrom = now.plusMinutes(29);
        LocalDateTime reminderWindowTo = now.plusMinutes(30);
        
        // Window T-15 mins (14 to 15) for Soft warning
        LocalDateTime endWindowFrom = now.plusMinutes(14);
        LocalDateTime endWindowTo = now.plusMinutes(15);

        List<Booking> bookings = bookingRepository.findBookingsNeedingAction(
                List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS),
                startWindowFrom, startWindowTo,
                reminderWindowFrom, reminderWindowTo,
                endWindowFrom, endWindowTo
        );

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                // Check if it's the start window for hardware lock
                if (booking.getStartTime().isAfter(startWindowFrom) && booking.getStartTime().isBefore(startWindowTo)) {
                    handlePreArrivalLock(booking);
                }
                // Check if it's the 30-min reminder window
                if (booking.getStartTime().isAfter(reminderWindowFrom) && booking.getStartTime().isBefore(reminderWindowTo)) {
                    handleBookingReminder(booking);
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
        PortStatus portStatus = resolvePortStatus(booking.getPortId());
        
        // Resolve portNumber for OCPP hardware communication
        Integer portNumber = resolvePortNumber(booking.getPortId());

        if (portStatus == PortStatus.CHARGING) {
            log.warn("Port {} (portId={}) is still charging (overstay). Sending RemoteStop.",
                    portNumber, booking.getPortId());
            eventPublisher.publishEvent(new SendRemoteStopCommandEvent(null, chargePointId, 0, "overstay"));
        }

        String idTag = "user-" + booking.getUserId();
        eventPublisher.publishEvent(new SendReserveNowCommandEvent(
                chargePointId, portNumber, idTag,
                booking.getEndTime(), booking.getId().intValue()));

        log.info("Pre-arrival lock dispatched: booking={}, chargePointId={}, portId={}",
                booking.getId(), chargePointId, booking.getPortId());
    }

    private void handleBookingReminder(Booking booking) {
        String formattedTime = booking.getStartTime()
                .atZone(java.time.ZoneId.of("UTC"))
                .withZoneSameInstant(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        eventPublisher.publishEvent(new SendPushNotificationEvent(
                booking.getUserId(),
                "Upcoming Charging Session \u23F0",
                "Your charging session is scheduled for " + formattedTime + ". Please arrive on time."
        ));
        log.info("Booking reminder sent: booking={}, userId={}", booking.getId(), booking.getUserId());
    }

    private void handleSoftWarning(Booking booking) {
        eventPublisher.publishEvent(new SendPushNotificationEvent(
                booking.getUserId(),
                "Charging Session Ending Soon",
                "Your charging session will end in 15 minutes. Please prepare to unplug."));
        log.info("Soft warning sent: booking={}, userId={}", booking.getId(), booking.getUserId());
    }

    private void handleHardCutoff(Booking booking) {
        // Issue 3: Only hard-cutoff if another booking follows on the same port.
        // If no upcoming booking exists, let the user keep charging.
        boolean hasNextBooking = bookingService.hasUpcomingBookingOnPort(
                booking.getPortId(), booking.getEndTime());

        if (!hasNextBooking) {
            log.info("No upcoming booking on portId={}. Allowing session to continue past booking end time.",
                    booking.getPortId());
            return;
        }

        String chargePointId = String.valueOf(booking.getChargerId());
        eventPublisher.publishEvent(new SendRemoteStopCommandEvent(null, chargePointId, 0, "hard-cutoff"));
        
        eventPublisher.publishEvent(new SendPushNotificationEvent(
                booking.getUserId(),
                "Charging Stopped \u26A0\uFE0F",
                "Your session has been safely stopped. Another user has a reservation. You have 10 minutes to move your vehicle."));
        
        log.info("Hard cut-off dispatched: booking={}, chargePointId={}, portId={}",
                booking.getId(), chargePointId, booking.getPortId());
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * Resolves the current port status using portId.
     */
    private PortStatus resolvePortStatus(Long portId) {
        return chargerService.findPortById(portId)
                .map(PortResponse::getStatus)
                .orElse(PortStatus.UNAVAILABLE);
    }

    /**
     * Resolves the OCPP connector number (portNumber) from the port database ID.
     * Used only for hardware communication.
     */
    private Integer resolvePortNumber(Long portId) {
        return chargerService.findPortById(portId)
                .map(PortResponse::getPortNumber)
                .orElse(0);
    }

    // ============================================================
    // Job 5: ZaloPay IPN Polling Fallback
    // Runs every 5 minutes. Queries PENDING invoices older than 15
    // minutes against the ZaloPay gateway to recover missed callbacks.
    // ============================================================
    @Scheduled(fixedRate = 300000)
    public void pollZaloPayPendingInvoices() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<InvoiceResponse> staleInvoices = invoiceService.findPendingOlderThan(threshold);

        if (staleInvoices.isEmpty()) {
            return;
        }

        log.info("ZaloPay fallback poll: found {} stale PENDING invoice(s)", staleInvoices.size());
        for (InvoiceResponse invoice : staleInvoices) {
            try {
                String appTransId = invoiceService.getLatestAppTransId(invoice.getId());
                if (appTransId == null) {
                    log.warn("No transaction found for invoiceId={}, skipping fallback poll", invoice.getId());
                    continue;
                }
                zaloPayService.queryOrderStatus(appTransId);
                log.info("ZaloPay fallback poll completed for invoiceId={}, appTransId={}",
                        invoice.getId(), appTransId);
            } catch (Exception e) {
                log.error("ZaloPay fallback poll failed for invoiceId={}: {}", invoice.getId(), e.getMessage());
            }
        }
    }
}
