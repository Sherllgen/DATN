package com.project.evgo.station.internal.web;

import com.project.evgo.station.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for station management.
 * Internal - not accessible by other modules, but exposed via HTTP.
 */
@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Tag(name = "Stations", description = "Station management APIs")
public class StationController {

    private final StationService stationService;
}
