package com.project.evgo.booking.internal;

import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.request.InvoiceCreatedRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.evgo.booking.request.CreateBookingRequest;
import com.project.evgo.booking.request.CheckAvailabilityRequest;
import com.project.evgo.sharedkernel.enums.BookingStatus;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.user.security.SecurityUtil;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.project.evgo.sharedkernel.dto.PageResponse;

/**
 * Implementation of BookingService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDtoConverter converter;
    private final InvoiceService invoiceService;
    private final PriceSettingService priceSettingService;
    private final StringRedisTemplate redisTemplate;

    private static final long LOCK_TTL_MINUTES = 8;
    private static final String LOCK_PREFIX = "evgo:booking:lock:";

    @Override
    public Optional<BookingResponse> findById(Long id) {
        return converter.toResponse(bookingRepository.findById(id));
    }

    @Override
    public List<BookingResponse> findByUserId(Long userId) {
        return converter.toResponseList(bookingRepository.findByUserId(userId));
    }

    @Override
    public List<BookingResponse> findByStationIdAndPortNumber(Long stationId, Integer portNumber) {
        return converter.toResponseList(bookingRepository.findByStationIdAndPortNumber(stationId, portNumber));
    }

    // check availability and create a hold on Redis
    @Override
    public void checkAvailability(CheckAvailabilityRequest request) {
        boolean hasOverlapDB = bookingRepository
                .existsByStationIdAndPortNumberAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
                        request.getStationId(),
                        request.getPortNumber(),
                        request.getStartTime(),
                        request.getEndTime(),
                        Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS));

        if (hasOverlapDB) {
            throw new AppException(ErrorCode.BOOKING_SLOT_UNAVAILABLE);
        }

        List<LocalDateTime> intervals = getIntervals(request.getStartTime(), request.getEndTime());
        List<String> lockedKeys = new ArrayList<>();
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // set lock for each 30-minute interval
        for (LocalDateTime interval : intervals) {
            String lockKey = generateLockKey(request.getStationId(), request.getPortNumber(), interval);
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, currentUserId.toString(),
                    LOCK_TTL_MINUTES, TimeUnit.MINUTES);

            if (Boolean.FALSE.equals(success)) {
                if (!lockedKeys.isEmpty()) {
                    redisTemplate.delete(lockedKeys);
                }
                throw new AppException(ErrorCode.BOOKING_SLOT_UNAVAILABLE);
            }
            lockedKeys.add(lockKey);
        }
    }

    // create booking with status PENDING and wait for confirmation (payment)
    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        List<LocalDateTime> intervals = getIntervals(request.getStartTime(), request.getEndTime());
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // check if the lock is still held by the user
        for (LocalDateTime interval : intervals) {
            String lockKey = generateLockKey(request.getStationId(), request.getPortNumber(), interval);
            String lockOwner = redisTemplate.opsForValue().get(lockKey);
            if (lockOwner == null || !lockOwner.equals(currentUserId.toString())) {
                throw new AppException(ErrorCode.BOOKING_SLOT_UNAVAILABLE);
            }
        }

        // calculate booking fee
        BigDecimal pricePerHour = priceSettingService.getActivePriceSetting(request.getStationId())
                .bookingFee();
        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        double durationHours = minutes / 60.0;
        BigDecimal estimatedCost = pricePerHour.multiply(BigDecimal.valueOf(durationHours));
        BigDecimal serviceFee = BigDecimal.ZERO; // Free or fixed

        Booking booking = new Booking();
        booking.setUserId(currentUserId);
        booking.setStationId(request.getStationId());
        booking.setChargerId(request.getChargerId());
        booking.setPortNumber(request.getPortNumber());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(estimatedCost);
        booking.setFee(serviceFee);

        Booking saved = bookingRepository.save(booking);

        InvoiceCreatedRequest req = new InvoiceCreatedRequest(saved.getId(), currentUserId, estimatedCost);
        invoiceService.createInvoice(req);

        return converter.toResponse(saved);
    }

    @Override
    public PageResponse<BookingResponse> getBookingsByStatus(String statusStr, int page, int size) {
        BookingStatus status;
        if ("UPCOMING".equalsIgnoreCase(statusStr)) {
            status = BookingStatus.CONFIRMED;
        } else if ("CANCELED".equalsIgnoreCase(statusStr)) {
            status = BookingStatus.CANCELLED;
        } else {
            try {
                status = BookingStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                status = BookingStatus.PENDING; // Fallback or reject
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage = bookingRepository.findByStatus(status, pageable);

        Page<BookingResponse> responsePage = bookingPage.map(converter::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED); // Already canceled
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED);
        }

        if (LocalDateTime.now().plusHours(2).isAfter(booking.getStartTime()) ||
                LocalDateTime.now().plusHours(2).isEqual(booking.getStartTime())) {
            throw new AppException(ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // splits the duration into 30-minute blocks
    private List<LocalDateTime> getIntervals(LocalDateTime startTime, LocalDateTime endTime) {
        List<LocalDateTime> intervals = new ArrayList<>();
        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            intervals.add(current);
            current = current.plusMinutes(30);
        }
        return intervals;
    }

    private String generateLockKey(Long stationId, Integer portNumber, LocalDateTime intervalStart) {
        return LOCK_PREFIX + stationId + ":" + portNumber + ":" + intervalStart.toString();
    }
}
