package com.project.evgo.booking.internal;

import com.project.evgo.booking.RequireRefundEvent;
import com.project.evgo.booking.SendReserveNowCommandEvent;
import com.project.evgo.payment.PaymentSuccessEvent;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        if (event.bookingId() != null) {
            log.info("Received PaymentSuccessEvent for bookingId: {}", event.bookingId());

            bookingRepository.findById(event.bookingId()).ifPresent(booking -> {
                if (booking.getStatus() == BookingStatus.CANCELLED) {
                    log.warn("Delayed IPN for cancelled booking: {}", booking.getId());
                    eventPublisher.publishEvent(new RequireRefundEvent(
                            booking.getId(),
                            booking.getUserId(),
                            booking.getTotalPrice(),
                            "Delayed IPN for cancelled booking"
                    ));
                    return;
                }

                // Check for silent overlap if the scheduler hasn't cancelled the PENDING block yet
                boolean hasOverlapDB = bookingRepository
                        .existsByStationIdAndPortNumberAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
                                booking.getStationId(),
                                booking.getPortNumber(),
                                booking.getStartTime(),
                                booking.getEndTime(),
                                java.util.Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS)
                        );
                
                if (hasOverlapDB) {
                    log.warn("Delayed IPN for booking {}; slot already taken by another user.", booking.getId());
                    booking.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    eventPublisher.publishEvent(new RequireRefundEvent(
                            booking.getId(),
                            booking.getUserId(),
                            booking.getTotalPrice(),
                            "Slot already taken by another user due to delayed payment"
                    ));
                    return;
                }

                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                LocalDateTime current = booking.getStartTime();
                while (current.isBefore(booking.getEndTime())) {
                    String lockKey = LOCK_PREFIX + booking.getStationId() + ":" + booking.getPortNumber() + ":" + current.toString();
                    redisTemplate.delete(lockKey);
                    current = current.plusMinutes(30);
                }
                
                log.info("Booking {} confirmed and Redis locks deleted.", booking.getId());

                // Immediate Hardware Reserve if booking starts within 10 minutes
                LocalDateTime now = LocalDateTime.now();
                if (booking.getStartTime().isBefore(now.plusMinutes(10)) && booking.getStartTime().isAfter(now)) {
                    log.info("Booking {} starts soon (within 10m). Dispatching immediate ReserveNow.", booking.getId());
                    eventPublisher.publishEvent(new SendReserveNowCommandEvent(
                            String.valueOf(booking.getChargerId()),
                            booking.getPortNumber(),
                            "user-" + booking.getUserId(),
                            booking.getEndTime(),
                            booking.getId().intValue()
                    ));
                }
            });
        }
    }
}
