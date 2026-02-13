package com.project.evgo.station;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.response.StationResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for admin station management.
 * Public API - accessible by other modules.
 */
public interface StationAdminService {

    /**
     * Get all stations with optional status filter.
     */
    PageResponse<StationResponse> getAllStations(StationStatus status, Pageable pageable);

    /**
     * Get station details by ID for admin review.
     */
    StationResponse getStationById(Long stationId);

    /**
     * Approve a pending station.
     */
    void approveStation(Long stationId);

    /**
     * Reject a pending station (set to SUSPENDED with reason).
     */
    void rejectStation(Long stationId, String reason);

    /**
     * Suspend an active station.
     */
    void suspendStation(Long stationId, String reason);

    /**
     * Unsuspend a suspended station (restore to ACTIVE).
     */
    void unsuspendStation(Long stationId);
}
