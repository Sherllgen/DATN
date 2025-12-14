package com.project.evgo.charger;

import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.SlotResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for charger and slot management.
 * Public API - accessible by other modules.
 */
public interface ChargerService {

    List<ChargerResponse> findByStationId(Long stationId);

    Optional<ChargerResponse> findById(Long id);

    List<SlotResponse> findSlotsByChargerId(Long chargerId);

    Optional<SlotResponse> findSlotById(Long id);
}
