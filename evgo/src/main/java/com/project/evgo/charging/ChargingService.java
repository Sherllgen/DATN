package com.project.evgo.charging;

import com.project.evgo.charging.request.StartChargingRequest;
import com.project.evgo.charging.request.StopChargingRequest;
import com.project.evgo.charging.response.ChargingSessionResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for charging session management.
 * Public API - accessible by other modules.
 */
public interface ChargingService {

    Optional<ChargingSessionResponse> findById(Long id);

    /**
     * @deprecated Use {@link #findActiveSession(Long)} or {@link #findSessionHistory(Long, ChargingSessionStatus, int, int)} instead.
     *             This method fetches ALL sessions for a user without pagination.
     */
    @Deprecated
    List<ChargingSessionResponse> findByUserId(Long userId);

    /**
     * Find the currently active charging session for a user, if any.
     * Looks for sessions in PREPARING, CHARGING, SUSPENDED_EV, SUSPENDED_EVSE, or FINISHING status.
     *
     * @param userId the authenticated user's ID
     * @return the active session, or empty if none
     */
    Optional<ChargingSessionResponse> findActiveSession(Long userId);

    /**
     * Find charging sessions for a user by status with server-side pagination.
     *
     * @param userId the authenticated user's ID
     * @param status the session status to filter by (e.g. COMPLETED)
     * @param page   zero-based page index
     * @param size   page size
     * @return a paginated response of charging sessions
     */
    PageResponse<ChargingSessionResponse> findSessionHistory(Long userId, ChargingSessionStatus status, int page, int size);

    ChargingSessionResponse startCharging(StartChargingRequest request, Long userId);

    void stopCharging(StopChargingRequest request, Long userId);
}
