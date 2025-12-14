package com.project.evgo.booking.internal;

import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.response.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of BookingService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDtoConverter converter;

    @Override
    public Optional<BookingResponse> findById(Long id) {
        return converter.toResponse(bookingRepository.findById(id));
    }

    @Override
    public List<BookingResponse> findByUserId(Long userId) {
        return converter.toResponseList(bookingRepository.findByUserId(userId));
    }

    @Override
    public List<BookingResponse> findBySlotId(Long slotId) {
        return converter.toResponseList(bookingRepository.findBySlotId(slotId));
    }
}
