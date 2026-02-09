package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.request.CreateStationRequest;
import com.project.evgo.station.request.UpdateStationRequest;
import com.project.evgo.station.response.StationResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for station management.
 * Public API - accessible by other modules.
 */
public interface StationService {

    Optional<StationResponse> findById(Long id);

    List<StationResponse> findAll();

    // Owner-specific operations
    StationResponse create(CreateStationRequest request);

    StationResponse update(Long id, UpdateStationRequest request);

    void delete(Long id);

    List<StationResponse> getMyStations();

    StationResponse updateStatus(Long id, StationStatus status);

    // Cross-module API
    /**
     * Verify current user owns the station. Throws AppException if not.
     * 
     * @param stationId Station ID to verify
     * @throws com.project.evgo.sharedkernel.exceptions.AppException if station not
     *                                                               found or not
     *                                                               owned
     */
    void verifyOwnership(Long stationId);

    /**
     * Check if current user owns the station.
     * 
     * @param stationId Station ID to check
     * @return true if current user owns the station
     */
    boolean isOwner(Long stationId);
}
