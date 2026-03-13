package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.request.CreateStationRequest;
import com.project.evgo.station.request.SearchNearbyRequest;
import com.project.evgo.station.request.SearchTextRequest;
import com.project.evgo.station.request.StationFilterRequest;
import com.project.evgo.station.request.UpdateStationRequest;
import com.project.evgo.station.response.StationMetadataResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
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

    List<StationSearchResult> findStationsInBound(Double minLat, Double maxLat, Double minLng, Double maxLng,
            Double userLat, Double userLng, Integer maxResults);

    // ==================== METADATA & FILTER ====================

    /**
     * Returns metadata for the station filter UI:
     * power range, distinct connector types, and all station statuses.
     */
    StationMetadataResponse getMetadata();

    /**
     * Returns filtered stations matching the optional criteria in the request.
     * Only non-null fields are applied as filters.
     */
    PageResponse<StationSearchResult> filterStations(StationFilterRequest request);
}
