package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.StationAdminService;
import com.project.evgo.station.response.StationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for admin station management.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StationAdminServiceImpl implements StationAdminService {

    private final StationRepository stationRepository;
    private final StationDtoConverter converter;

    @Override
    public PageResponse<StationResponse> getAllStations(StationStatus status, Pageable pageable) {
        Page<Station> stationPage;

        if (status != null) {
            stationPage = stationRepository.findByStatusAndDeletedAtIsNull(status, pageable);
        } else {
            stationPage = stationRepository.findByDeletedAtIsNull(pageable);
        }

        Page<StationResponse> responsePage = stationPage.map(converter::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public StationResponse getStationById(Long stationId) {
        Station station = stationRepository.findByIdAndDeletedAtIsNull(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        return converter.toResponse(station);
    }

    @Override
    @Transactional
    public void approveStation(Long stationId) {
        Station station = stationRepository.findByIdAndDeletedAtIsNull(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (station.getStatus() != StationStatus.PENDING) {
            throw new AppException(ErrorCode.STATION_ALREADY_APPROVED);
        }

        station.setStatus(StationStatus.ACTIVE);
        stationRepository.save(station);

        log.info("Station {} approved", stationId);
    }

    @Override
    @Transactional
    public void rejectStation(Long stationId, String reason) {
        Station station = stationRepository.findByIdAndDeletedAtIsNull(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (station.getStatus() != StationStatus.PENDING) {
            throw new AppException(ErrorCode.STATION_INVALID_STATUS_CHANGE,
                    "Can only reject stations with PENDING status");
        }

        station.setStatus(StationStatus.SUSPENDED);
        // Note: If you want to store rejection reason, add a field to Station entity
        stationRepository.save(station);

        log.info("Station {} rejected with reason: {}", stationId, reason);
    }

    @Override
    @Transactional
    public void suspendStation(Long stationId, String reason) {
        Station station = stationRepository.findByIdAndDeletedAtIsNull(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (station.getStatus() == StationStatus.SUSPENDED) {
            throw new AppException(ErrorCode.STATION_ALREADY_SUSPENDED);
        }

        if (station.getStatus() == StationStatus.PENDING) {
            throw new AppException(ErrorCode.STATION_INVALID_STATUS_CHANGE,
                    "Cannot suspend a pending station. Use reject instead.");
        }

        station.setStatus(StationStatus.SUSPENDED);
        stationRepository.save(station);

        log.info("Station {} suspended with reason: {}", stationId, reason);
    }

    @Override
    @Transactional
    public void unsuspendStation(Long stationId) {
        Station station = stationRepository.findByIdAndDeletedAtIsNull(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (station.getStatus() != StationStatus.SUSPENDED) {
            throw new AppException(ErrorCode.STATION_INVALID_STATUS_CHANGE,
                    "Can only unsuspend stations with SUSPENDED status");
        }

        station.setStatus(StationStatus.ACTIVE);
        stationRepository.save(station);

        log.info("Station {} unsuspended", stationId);
    }
}
