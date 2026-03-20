package com.project.evgo.booking.internal;

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
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                String lockKey = LOCK_PREFIX + booking.getStationId() + ":" + booking.getPortNumber() + ":" + booking.getStartTime();
                redisTemplate.delete(lockKey);
                
                log.info("Booking {} confirmed and Redis lock deleted.", booking.getId());

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
