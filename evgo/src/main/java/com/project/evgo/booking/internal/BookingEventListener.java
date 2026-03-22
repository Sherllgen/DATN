package com.project.evgo.booking.internal;

import com.project.evgo.booking.BookingConfirmedAndReadyForHardwareEvent;
import com.project.evgo.booking.RequireRefundEvent;
import com.project.evgo.booking.SendReserveNowCommandEvent;
import com.project.evgo.payment.PaymentSuccessEvent;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener for booking-related events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final BookingRepository bookingRepository;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    
    private static final String LOCK_PREFIX = "evgo:booking:lock:";

    @EventListener
    @Transactional
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        if (event.bookingId() == null) return;

        log.info("Received PaymentSuccessEvent for bookingId: {}", event.bookingId());

        bookingRepository.findById(event.bookingId()).ifPresent(booking -> {
            // 1. Handle delayed IPN: Booking has been cancelled before
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                log.warn("Delayed IPN for cancelled booking: {}", booking.getId());
                publishRefundEvent(booking, "Delayed IPN for cancelled booking");
                return;
            }

            // 2. Handle delayed IPN: Slot has been taken by another user
            boolean hasOverlapDB = bookingRepository.existsByStationIdAndPortNumberAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
                    booking.getStationId(), booking.getPortNumber(),
                    booking.getStartTime(), booking.getEndTime(),
                    java.util.Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)
            );

            if (hasOverlapDB) {
                log.warn("Delayed IPN for booking {}; slot already taken by another user.", booking.getId());
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                publishRefundEvent(booking, "Slot already taken by another user due to delayed payment");
                return;
            }

            // 3. Confirm Booking
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // 4. Delete Redis Keys in Batch
            List<String> keysToDelete = new ArrayList<>();
            LocalDateTime current = booking.getStartTime();
            
            while (current.isBefore(booking.getEndTime())) {
                String timeStr = current.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                keysToDelete.add(LOCK_PREFIX + booking.getStationId() + ":" + booking.getPortNumber() + ":" + timeStr);
                current = current.plusMinutes(30); 
            }
            
            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete); 
            }
            
            log.info("Booking {} confirmed and {} Redis locks deleted.", booking.getId(), keysToDelete.size());

            // 5. Send hardware command 
            LocalDateTime now = LocalDateTime.now();
            if (booking.getStartTime().isBefore(now.plusMinutes(10)) && booking.getStartTime().isAfter(now)) {
                log.info("Booking {} starts soon (within 10m). Dispatching immediate ReserveNow.", booking.getId());
                eventPublisher.publishEvent(new BookingConfirmedAndReadyForHardwareEvent(booking));
            }
        });
    }

    private void publishRefundEvent(Booking booking, String reason) {
        eventPublisher.publishEvent(new RequireRefundEvent(
                booking.getId(), booking.getUserId(), booking.getTotalPrice(), reason
        ));
    }

    // Spring automatic call this after transaction commit
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmedReadyForHardware(BookingConfirmedAndReadyForHardwareEvent event) {
        Booking booking = event.booking();
        eventPublisher.publishEvent(new SendReserveNowCommandEvent(
                String.valueOf(booking.getChargerId()),
                booking.getPortNumber(),
                "user-" + booking.getUserId(),
                booking.getEndTime(),
                booking.getId().intValue()
        ));
        log.info("Hardware ReserveNow safely dispatched for booking {}", booking.getId());
    }
}
