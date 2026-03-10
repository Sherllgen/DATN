package com.project.evgo.station.internal;

import com.project.evgo.station.response.PriceSettingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for PriceSetting entity to PriceSettingResponse DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
public class PriceSettingDtoConverter {

    public PriceSettingDtoConverter() {
    }

    public PriceSettingResponse convert(PriceSetting from) {
        return PriceSettingResponse.builder()
                .id(from.getId())
                .stationId(from.getStationId())
                .version(from.getVersion())
                .chargingRatePerKwh(from.getChargingRatePerKwh())
                .bookingFee(from.getBookingFee())
                .idlePenaltyPerMinute(from.getIdlePenaltyPerMinute())
                .gracePeriodMinutes(from.getGracePeriodMinutes())
                .isActive(from.getIsActive())
                .notes(from.getNotes())
                .effectiveFrom(from.getEffectiveFrom())
                .createdAt(from.getCreatedAt())
                .build();
    }

    public List<PriceSettingResponse> convert(List<PriceSetting> from) {
        return from.stream().map(this::convert).toList();
    }

    public Optional<PriceSettingResponse> convert(Optional<PriceSetting> from) {
        return from.map(this::convert);
    }
}
