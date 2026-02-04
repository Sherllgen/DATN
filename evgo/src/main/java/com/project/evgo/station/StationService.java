package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.request.CreateStationRequest;
import com.project.evgo.station.request.SearchNearbyRequest;
import com.project.evgo.station.request.SearchTextRequest;
import com.project.evgo.station.request.UpdateStationRequest;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.station.response.StationSearchResult;

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

    List<StationSearchResult> searchNearby(SearchNearbyRequest request);

    List<StationSearchResult> searchByText(SearchTextRequest request);
}
