package com.project.evgo.station.internal;

import com.project.evgo.station.StationService;
import com.project.evgo.station.response.StationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return stationDtoConverter.convert(stationRepository.findById(id));
    }

    @Override
    public List<StationResponse> findAll() {
        return stationDtoConverter.convert(stationRepository.findAll());
    }
}
