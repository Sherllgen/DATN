package com.project.evgo.booking;

import com.project.evgo.booking.request.CreateBookingRequest;
import com.project.evgo.booking.request.CheckAvailabilityRequest;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.booking.response.BookingStatsResponse;
import com.project.evgo.booking.response.OwnerBookingSummaryResponse;
import com.project.evgo.payment.response.InvoiceStatsResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;

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

    List<BookingResponse> findByStationIdAndPortNumber(Long stationId, Integer portNumber);

    void checkAvailability(CheckAvailabilityRequest request);

    BookingResponse createBooking(CreateBookingRequest request);

    PageResponse<BookingResponse> getBookingsByStatus(String statusStr, int page, int size);

    PageResponse<OwnerBookingSummaryResponse> getOwnerBookings(Long ownerId, Pageable pageable);

    List<Long> getBookingIdsByOwnerId(Long ownerId);

    BookingStatsResponse getOwnerStats(Long ownerId);

    InvoiceStatsResponse getOwnerInvoiceStats(Long ownerId);

    void cancelBooking(Long id);
}
