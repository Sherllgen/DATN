package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.StationService;
import com.project.evgo.station.request.CreateStationRequest;
import com.project.evgo.station.request.SearchNearbyRequest;
import com.project.evgo.station.request.SearchTextRequest;
import com.project.evgo.station.request.UpdateStationRequest;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.station.response.StationSearchResult;
import com.project.evgo.user.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of StationService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final StationDtoConverter stationDtoConverter;

    @Override
    public Optional<StationResponse> findById(Long id) {
        return stationDtoConverter.convert(stationRepository.findByIdAndDeletedAtIsNull(id));
    }

    @Override
    public List<StationResponse> findAll() {
        return stationDtoConverter.convert(stationRepository.findAllByDeletedAtIsNull());
    }

    @Override
    @Transactional
    public StationResponse create(CreateStationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        Long ownerId = SecurityUtil.getCurrentUserId();

        // Check if station name already exists for this owner
        if (stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull(request.name(), ownerId)) {
            throw new AppException(ErrorCode.STATION_NAME_ALREADY_EXISTS);
        }

        Station station = new Station();
        station.setOwnerId(ownerId);
        station.setName(request.name());
        station.setDescription(request.description());
        station.setAddress(request.address());
        station.setLatitude(request.latitude());
        station.setLongitude(request.longitude());
        station.setStatus(StationStatus.PENDING);
        if (request.imageUrls() != null) {
            station.setImageUrls(request.imageUrls());
        }

        if (request.openingHours() != null) {
            List<StationOpeningHours> hours = request.openingHours().stream()
                    .map(h -> {
                        StationOpeningHours soh = new StationOpeningHours();
                        soh.setStation(station);
                        soh.setDayOfWeek(h.dayOfWeek());
                        soh.setOpenTime(h.openTime());
                        soh.setCloseTime(h.closeTime());
                        soh.setIsOpen(h.isOpen());
                        return soh;
                    }).toList();
            station.setOpeningHours(hours);
        }

        Station saved = stationRepository.save(station);
        return stationDtoConverter.convert(saved);
    }

    @Override
    @Transactional
    public StationResponse update(Long id, UpdateStationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();

        Station station = stationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        // Check ownership
        if (!station.getOwnerId().equals(currentUserId)) {
            throw new AppException(ErrorCode.STATION_NOT_OWNED);
        }

        // Check for duplicate name (excluding current station)
        if (stationRepository.existsByNameAndOwnerIdAndIdNotAndDeletedAtIsNull(
                request.name(), currentUserId, id)) {
            throw new AppException(ErrorCode.STATION_NAME_ALREADY_EXISTS);
        }

        station.setName(request.name());
        station.setDescription(request.description());
        station.setAddress(request.address());
        station.setLatitude(request.latitude());
        station.setLongitude(request.longitude());
        if (request.imageUrls() != null) {
            station.setImageUrls(request.imageUrls());
        }

        if (request.openingHours() != null) {
            station.getOpeningHours().clear();
            List<StationOpeningHours> hours = request.openingHours().stream()
                    .map(h -> {
                        StationOpeningHours soh = new StationOpeningHours();
                        soh.setStation(station);
                        soh.setDayOfWeek(h.dayOfWeek());
                        soh.setOpenTime(h.openTime());
                        soh.setCloseTime(h.closeTime());
                        soh.setIsOpen(h.isOpen());
                        return soh;
                    }).toList();
            station.getOpeningHours().addAll(hours);
        }

        Station updated = stationRepository.save(station);
        return stationDtoConverter.convert(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Station station = stationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        // Check ownership
        if (!station.getOwnerId().equals(currentUserId)) {
            throw new AppException(ErrorCode.STATION_NOT_OWNED);
        }

        // Soft delete
        station.setDeletedAt(LocalDateTime.now());
        stationRepository.save(station);
    }

    @Override
    public List<StationResponse> getMyStations() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<Station> stations = stationRepository.findByOwnerIdAndDeletedAtIsNull(currentUserId);
        return stationDtoConverter.convert(stations);
    }

    @Override
    @Transactional
    public StationResponse updateStatus(Long id, StationStatus status) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Station station = stationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        // Check ownership
        if (!station.getOwnerId().equals(currentUserId)) {
            throw new AppException(ErrorCode.STATION_NOT_OWNED);
        }

        station.setStatus(status);
        Station updated = stationRepository.save(station);
        return stationDtoConverter.convert(updated);
    }

    // ==================== CROSS-MODULE API ====================

    @Override
    public void verifyOwnership(Long stationId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Station station = stationRepository.findByIdAndDeletedAtIsNull(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (!station.getOwnerId().equals(currentUserId)) {
            throw new AppException(ErrorCode.STATION_NOT_OWNED);
        }
    }

    @Override
    public boolean isOwner(Long stationId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        return stationRepository.findByIdAndDeletedAtIsNull(stationId)
                .map(station -> station.getOwnerId().equals(currentUserId))
                .orElse(false);
    }

    @Override
    public List<StationSearchResult> searchNearby(SearchNearbyRequest request) {
        // Validate inputs
        if (request.latitude() == null || request.longitude() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Latitude and longitude are required");
        }

        // Convert radius from km to meters
        double radiusMeters = request.radiusKm() * 1000;

        // Query database using optimized PostGIS query
        List<StationProjection> projections = stationRepository.findNearByStations(
                request.latitude(),
                request.longitude(),
                radiusMeters,
                request.maxResults());

        // Convert to DTOs with charger counts
        return stationDtoConverter.convertToSearchResults(projections);
    }

    @Override
    public List<StationSearchResult> searchByText(SearchTextRequest request) {
        // Validate query
        if (request.query() == null || request.query().isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Search query is required");
        }

        // Query database
        List<StationProjection> projections = stationRepository.searchByText(
                request.query().trim(),
                request.latitude(),
                request.longitude(),
                request.maxResults());

        // Convert to DTOs with charger counts
        return stationDtoConverter.convertToSearchResults(projections);
    }

    @Override
    public List<StationSearchResult> findStationsInBound(Double minLat, Double maxLat, Double minLng, Double maxLng,
            Double userLat, Double userLng, Integer maxResults) {
        // Validate bounding box coordinates
        if (minLat == null || maxLat == null || minLng == null || maxLng == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "All bounding box coordinates are required");
        }

        if (minLat >= maxLat) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "minLat must be less than maxLat");
        }

        if (minLng >= maxLng) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "minLng must be less than maxLng");
        }

        // Query database using PostGIS spatial query with optional distance calculation
        List<StationProjection> projections = stationRepository.findStationsInBound(
                minLat, maxLat, minLng, maxLng, userLat, userLng, maxResults);

        // Convert to DTOs with charger counts
        return stationDtoConverter.convertToSearchResults(projections);
    }
}
