package com.project.evgo.charging;

import com.project.evgo.charging.request.StartChargingRequest;
import com.project.evgo.charging.request.StopChargingRequest;
import com.project.evgo.charging.response.ChargingSessionResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for charging session management.
 * Public API - accessible by other modules.
 */
public interface ChargingService {

    Optional<ChargingSessionResponse> findById(Long id);

    // Optional<ChargingSessionResponse> findByBookingId(Long bookingId);

    List<ChargingSessionResponse> findByUserId(Long userId);

    ChargingSessionResponse startCharging(StartChargingRequest request, Long userId);

    void stopCharging(StopChargingRequest request, Long userId);
}
