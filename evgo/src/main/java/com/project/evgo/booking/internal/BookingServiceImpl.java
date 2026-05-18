package com.project.evgo.booking.internal;

import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.booking.response.BookingStatsResponse;
import com.project.evgo.booking.response.MonthlyChartEntry;
import com.project.evgo.booking.response.OwnerBookingSummaryResponse;
import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceStatsResponse;
import com.project.evgo.station.StationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.project.evgo.sharedkernel.dto.PageResponse;

/**
 * Implementation of BookingService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDtoConverter converter;
    private final InvoiceService invoiceService;
    private final PriceSettingService priceSettingService;
    private final StringRedisTemplate redisTemplate;
    private final StationService stationService;

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
    public List<BookingResponse> findByPortId(Long portId) {
        return converter.toResponseList(bookingRepository.findByPortId(portId));
    }

    // check availability and create a hold on Redis
    @Override
    public void checkAvailability(CheckAvailabilityRequest request) {
        boolean hasOverlapDB = bookingRepository
                .existsByPortIdAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
                        request.getPortId(),
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
            String lockKey = generateLockKey(request.getPortId(), interval);
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
            String lockKey = generateLockKey(request.getPortId(), interval);
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
        booking.setVehicleId(request.getVehicleId());
        booking.setPortId(request.getPortId());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(estimatedCost);
        booking.setFee(serviceFee);

        Booking saved = bookingRepository.save(booking);

        // B4: Never create a PENDING invoice for 0 VND — it would permanently block the user
        // from starting a new charging session (hasUnpaidInvoices check) without any way to pay.
        if (estimatedCost.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceCreatedRequest req = new InvoiceCreatedRequest(saved.getId(), currentUserId, estimatedCost);
            invoiceService.createInvoice(req);
        } else {
            log.warn("Booking {} has zero estimated cost — no invoice created.", saved.getId());
        }

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
    public PageResponse<OwnerBookingSummaryResponse> getOwnerBookings(Long ownerId, Pageable pageable) {
        List<Long> stationIds = stationService.getStationIdsByOwnerId(ownerId);
        if (stationIds.isEmpty()) {
            return PageResponse.of(new PageImpl<>(List.of(), pageable, 0));
        }
        Page<Booking> bookingPage = bookingRepository.findByStationIdIn(stationIds, pageable);
        List<OwnerBookingSummaryResponse> responses = converter.toOwnerSummaryListBulk(bookingPage.getContent());
        return PageResponse.of(new PageImpl<>(responses, pageable, bookingPage.getTotalElements()));
    }

    @Override
    public BookingStatsResponse getOwnerStats(Long ownerId) {
        List<Long> stationIds = stationService.getStationIdsByOwnerId(ownerId);
        if (stationIds.isEmpty()) {
            return new BookingStatsResponse(0, 0, 0.0, 0.0);
        }

        List<BookingStatus> activeStatuses = List.of(BookingStatus.COMPLETED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);

        long totalBookings = bookingRepository.countByStationIdInAndStatusIn(stationIds, activeStatuses);

        long totalCustomers = bookingRepository.countDistinctUserIdByStationIdInAndStatusIn(stationIds, activeStatuses);

        return BookingStatsResponse.builder()
                .totalBookings(totalBookings)
                .totalCustomers(totalCustomers)
                .bookingsGrowth(0.0) // Future implementation
                .customersGrowth(0.0) // Future implementation
                .build();
    }

    @Override
    public List<Long> getBookingIdsByOwnerId(Long ownerId) {
        List<Long> stationIds = stationService.getStationIdsByOwnerId(ownerId);
        if (stationIds.isEmpty())
            return List.of();
        return bookingRepository.findIdsByStationIdIn(stationIds);
    }

    @Override
    public InvoiceStatsResponse getOwnerInvoiceStats(Long ownerId) {
        List<Long> bookingIds = getBookingIdsByOwnerId(ownerId);
        return invoiceService.getStatsByBookingIds(bookingIds);
    }

    @Override
    public List<MonthlyChartEntry> getOwnerMonthlyChart(Long ownerId) {
        int year = LocalDate.now().getYear();
        List<Long> stationIds = stationService.getStationIdsByOwnerId(ownerId);
        if (stationIds.isEmpty()) {
            return buildEmptyChart();
        }

        List<Long> bookingIds = bookingRepository.findIdsByStationIdIn(stationIds);

        // Monthly booking counts (SUCCESS only)
        List<Object[]> bookingRows = bookingRepository.countMonthlyByStationIdsAndStatusAndYear(
                stationIds, BookingStatus.COMPLETED, year);
        Map<Integer, Long> bookingsByMonth = new HashMap<>();
        for (Object[] row : bookingRows) {
            bookingsByMonth.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        // Monthly revenue (PAID invoices)
        Map<Integer, BigDecimal> revenueByMonth = invoiceService.getMonthlyRevenueByBookingIds(bookingIds, year);

        // Build 12-month result
        List<MonthlyChartEntry> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String monthName = java.time.Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(MonthlyChartEntry.builder()
                    .month(monthName)
                    .bookings(bookingsByMonth.getOrDefault(m, 0L))
                    .revenue(revenueByMonth.getOrDefault(m, BigDecimal.ZERO))
                    .build());
        }
        return result;
    }

    private List<MonthlyChartEntry> buildEmptyChart() {
        List<MonthlyChartEntry> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String monthName = java.time.Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(MonthlyChartEntry.builder()
                    .month(monthName)
                    .bookings(0L)
                    .revenue(BigDecimal.ZERO)
                    .build());
        }
        return result;
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

        // Cancel the linked PENDING invoice so the user is not left with unpayable debt.
        invoiceService.cancelInvoiceByBookingId(id);
        log.info("Booking {} cancelled and linked invoice cancelled.", id);
    }

    @Override
    @Transactional
    public void cancelStalePendingBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            invoiceService.cancelInvoiceByBookingId(bookingId);
            log.info("Cancelled stale PENDING booking {} and its linked invoice.", bookingId);
        }
    }

    @Override
    @Transactional
    public void startBookingSession(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Booking must be CONFIRMED to start. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.IN_PROGRESS);
        bookingRepository.save(booking);
    }

    @Override
    public List<Long> getPortsWithUpcomingBookings(List<Long> portIds, LocalDateTime after) {
        if (portIds == null || portIds.isEmpty()) return List.of();
        return bookingRepository.findPortIdsWithUpcomingBookings(portIds, BookingStatus.CONFIRMED, after);
    }

    @Override
    @Transactional
    public void revertBookingToConfirmed(Long bookingId) {
        if (bookingId == null) {
            return;
        }
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                log.info("Booking {} reverted from IN_PROGRESS to CONFIRMED after INTERRUPTED session.", bookingId);
            } else {
                log.debug("Booking {} is {} — no revert needed.", bookingId, booking.getStatus());
            }
        });
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

    private String generateLockKey(Long portId, LocalDateTime intervalStart) {
        return LOCK_PREFIX + portId + ":" + intervalStart.toString();
    }
}
