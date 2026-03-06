package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.StationOwnershipValidator;
import com.project.evgo.user.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates ownership of a station.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationOwnershipValidatorImpl implements StationOwnershipValidator {

    private final StationRepository stationRepository;

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
}
