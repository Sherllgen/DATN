package com.project.evgo.booking;

import com.project.evgo.booking.request.CreateBookingRequest;
import com.project.evgo.booking.request.CheckAvailabilityRequest;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;

import java.util.List;
import java.util.Optional;

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

    void cancelBooking(Long id);
}
