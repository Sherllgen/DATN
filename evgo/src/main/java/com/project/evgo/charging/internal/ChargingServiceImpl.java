package com.project.evgo.charging.internal;

import com.project.evgo.charging.ChargingService;
import com.project.evgo.charging.response.ChargingSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ChargingService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargingServiceImpl implements ChargingService {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingDtoConverter converter;

    @Override
    public Optional<ChargingSessionResponse> findById(Long id) {
        return converter.toResponse(sessionRepository.findById(id));
    }

    // @Override
    // public Optional<ChargingSessionResponse> findByBookingId(Long bookingId) {
    //     return converter.toResponse(sessionRepository.findByBookingId(bookingId));
    // }

    @Override
    public List<ChargingSessionResponse> findByUserId(Long userId) {
        return converter.toResponseList(sessionRepository.findByUserId(userId));
    }
}
