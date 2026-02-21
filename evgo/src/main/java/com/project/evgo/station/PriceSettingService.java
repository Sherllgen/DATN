package com.project.evgo.station;

import com.project.evgo.station.request.CreatePriceSettingRequest;
import com.project.evgo.station.response.PriceSettingResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for station pricing management.
 * Public API - accessible by other modules.
 */
public interface PriceSettingService {

    /**
     * Create a new pricing version for a station.
     * Deactivates the current active version and creates a new one.
     *
     * @param stationId Station ID
     * @param request   Pricing details
     * @return Created pricing response
     */
    PriceSettingResponse createPriceSetting(Long stationId, CreatePriceSettingRequest request);

    /**
     * Get the current active pricing for a station.
     *
     * @param stationId Station ID
     * @return Active pricing response
     */
    PriceSettingResponse getActivePriceSetting(Long stationId);

    /**
     * Get full pricing history for a station, ordered by version desc.
     *
     * @param stationId Station ID
     * @return List of all pricing versions
     */
    List<PriceSettingResponse> getPricingHistory(Long stationId);

    /**
     * Calculate the idle fee based on overstay minutes.
     * Formula: max(0, overstayMinutes - gracePeriodMinutes) * idlePenaltyPerMinute
     *
     * @param stationId       Station ID
     * @param overstayMinutes Total minutes since charging completed
     * @return Calculated idle fee
     */
    BigDecimal calculateIdleFee(Long stationId, int overstayMinutes);
}
