package com.project.evgo.charger.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
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
    private final PortRepository portRepository;
    private final ChargerDtoConverter converter;

    @Override
    public List<ChargerResponse> findByStationId(Long stationId) {
        return converter.toChargerResponse(chargerRepository.findByStationId(stationId));
    }

    @Override
    public Optional<ChargerResponse> findById(Long id) {
        return converter.toChargerResponse(chargerRepository.findById(id));
    }

    @Override
    public List<PortResponse> findPortsByChargerId(Long chargerId) {
        return converter.toPortResponse(portRepository.findByChargerId(chargerId));
    }

    @Override
    public Optional<PortResponse> findPortById(Long id) {
        return converter.toPortResponse(portRepository.findById(id));
    }
}
