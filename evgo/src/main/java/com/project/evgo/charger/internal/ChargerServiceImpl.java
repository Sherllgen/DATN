package com.project.evgo.charger.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.SlotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ChargerService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargerServiceImpl implements ChargerService {

    private final ChargerRepository chargerRepository;
    private final SlotRepository slotRepository;
    private final ChargerDtoConverter converter;

    @Override
    public List<ChargerResponse> findByStationId(Long stationId) {
        return converter.toChargerResponseList(chargerRepository.findByStationId(stationId));
    }

    @Override
    public Optional<ChargerResponse> findById(Long id) {
        return converter.toChargerResponse(chargerRepository.findById(id));
    }

    @Override
    public List<SlotResponse> findSlotsByChargerId(Long chargerId) {
        return converter.toSlotResponseList(slotRepository.findByChargerId(chargerId));
    }

    @Override
    public Optional<SlotResponse> findSlotById(Long id) {
        return converter.toSlotResponse(slotRepository.findById(id));
    }
}
