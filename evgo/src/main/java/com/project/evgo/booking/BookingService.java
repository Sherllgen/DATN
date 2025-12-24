package com.project.evgo.booking;

import com.project.evgo.booking.response.BookingResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for booking management.
 * Public API - accessible by other modules.
 */
public interface BookingService {

    Optional<BookingResponse> findById(Long id);

    List<BookingResponse> findByUserId(Long userId);

    List<BookingResponse> findByPortId(Long portId);
}
