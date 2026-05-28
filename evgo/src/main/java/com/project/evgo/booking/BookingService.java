package com.project.evgo.booking;

import com.project.evgo.booking.request.CreateBookingRequest;
import com.project.evgo.booking.request.CheckAvailabilityRequest;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.booking.response.BookingStatsResponse;
import com.project.evgo.booking.response.OwnerBookingSummaryResponse;
import com.project.evgo.payment.response.InvoiceStatsResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;

import com.project.evgo.booking.response.MonthlyChartEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

/**
 * Service interface for booking management.
 * Public API - accessible by other modules.
 */
public interface BookingService {

    Optional<BookingResponse> findById(Long id);

    List<BookingResponse> findByUserId(Long userId);

    List<BookingResponse> findByPortId(Long portId);

    void checkAvailability(CheckAvailabilityRequest request);

    BookingResponse createBooking(CreateBookingRequest request);

    PageResponse<BookingResponse> getBookingsByStatus(String statusStr, int page, int size);

    PageResponse<OwnerBookingSummaryResponse> getOwnerBookings(Long ownerId, Pageable pageable);

    List<Long> getBookingIdsByOwnerId(Long ownerId);

    BookingStatsResponse getOwnerStats(Long ownerId);

    InvoiceStatsResponse getOwnerInvoiceStats(Long ownerId);

    List<MonthlyChartEntry> getOwnerMonthlyChart(Long ownerId);

    void cancelBooking(Long id);

    /**
     * Called by the system scheduler to cancel a stale PENDING booking
     */
    void cancelStalePendingBooking(Long bookingId);

    /**
     * Called by the charging module when a user starts a charging session from a booking.
     */
    void startBookingSession(Long bookingId);

    /**
     * Bulk check whether there is an upcoming CONFIRMED booking on the given ports
     * starting after the given time.
     *
     * @return list of portIds that have upcoming bookings
     */
    List<Long> getPortsWithUpcomingBookings(List<Long> portIds, LocalDateTime after);

    /**
     * Reverts an IN_PROGRESS booking back to CONFIRMED status.
     */
    void revertBookingToConfirmed(Long bookingId);

    /**
     * Transitions a no-show CONFIRMED booking to EXPIRED status.
     */
    void expireBooking(Long bookingId);
}
