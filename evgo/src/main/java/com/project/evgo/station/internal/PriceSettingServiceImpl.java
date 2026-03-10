package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.station.StationOwnershipValidator;
import com.project.evgo.station.request.CreatePriceSettingRequest;
import com.project.evgo.station.response.PriceSettingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of PriceSettingService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
public class PriceSettingServiceImpl implements PriceSettingService {

    private static final int DEFAULT_GRACE_PERIOD_MINUTES = 30;

    private final PriceSettingRepository priceSettingRepository;
    private final PriceSettingDtoConverter priceSettingDtoConverter;
    private final StationOwnershipValidator stationOwnershipValidator;

    @Override
    @Transactional
    public PriceSettingResponse createPriceSetting(Long stationId, CreatePriceSettingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Verify station exists and current user owns it
        stationOwnershipValidator.verifyOwnership(stationId);

        // Validate prices are positive
        if (request.chargingRatePerKwh().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE_VALUE, "Charging rate must be positive");
        }
        if (request.bookingFee() != null && request.bookingFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_PRICE_VALUE, "Booking fee cannot be negative");
        }
        if (request.idlePenaltyPerMinute() != null && request.idlePenaltyPerMinute().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_PRICE_VALUE, "Idle penalty cannot be negative");
        }

        // Determine next version number
        int nextVersion = priceSettingRepository.findTopByStationIdOrderByVersionDesc(stationId)
                .map(existing -> existing.getVersion() + 1)
                .orElse(1);

        // Deactivate all current active pricing
        priceSettingRepository.deactivateAllByStationId(stationId);

        // Create new version
        PriceSetting priceSetting = new PriceSetting();
        priceSetting.setStationId(stationId);
        priceSetting.setVersion(nextVersion);
        priceSetting.setChargingRatePerKwh(request.chargingRatePerKwh());
        priceSetting.setBookingFee(request.bookingFee());
        priceSetting.setIdlePenaltyPerMinute(request.idlePenaltyPerMinute());
        priceSetting.setGracePeriodMinutes(
                request.gracePeriodMinutes() != null ? request.gracePeriodMinutes() : DEFAULT_GRACE_PERIOD_MINUTES);
        priceSetting.setIsActive(true);
        priceSetting.setNotes(request.notes());
        priceSetting.setEffectiveFrom(
                request.effectiveFrom() != null ? request.effectiveFrom() : LocalDateTime.now());

        PriceSetting saved = priceSettingRepository.save(priceSetting);
        return priceSettingDtoConverter.convert(saved);
    }

    @Override
    public PriceSettingResponse getActivePriceSetting(Long stationId) {
        PriceSetting active = priceSettingRepository.findByStationIdAndIsActiveTrue(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICE_SETTING_NOT_FOUND));
        return priceSettingDtoConverter.convert(active);
    }

    @Override
    public List<PriceSettingResponse> getPricingHistory(Long stationId) {
        // Verify ownership for viewing history
        stationOwnershipValidator.verifyOwnership(stationId);

        List<PriceSetting> history = priceSettingRepository.findByStationIdOrderByVersionDesc(stationId);
        return priceSettingDtoConverter.convert(history);
    }

    @Override
    public BigDecimal calculateIdleFee(Long stationId, int overstayMinutes) {
        PriceSetting active = priceSettingRepository.findByStationIdAndIsActiveTrue(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICE_SETTING_NOT_FOUND));

        if (active.getIdlePenaltyPerMinute() == null
                || active.getIdlePenaltyPerMinute().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        int billableMinutes = Math.max(0, overstayMinutes - active.getGracePeriodMinutes());
        return active.getIdlePenaltyPerMinute().multiply(BigDecimal.valueOf(billableMinutes));
    }
}
