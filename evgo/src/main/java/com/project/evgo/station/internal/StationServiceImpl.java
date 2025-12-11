package com.project.evgo.station.internal;

import com.project.evgo.station.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of StationService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
}
