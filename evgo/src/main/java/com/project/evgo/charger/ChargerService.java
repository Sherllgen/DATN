package com.project.evgo.charger;

import com.project.evgo.charger.request.CreateChargerRequest;
import com.project.evgo.charger.request.CreatePortRequest;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.sharedkernel.enums.PortStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for charger and port management.
 * Public API - accessible by other modules.
 */
public interface ChargerService {

    // Read operations
    List<ChargerResponse> findByStationId(Long stationId);

    Optional<ChargerResponse> findById(Long id);

    List<PortResponse> findPortsByChargerId(Long chargerId);

    Optional<PortResponse> findPortById(Long id);

    // Charger management (Owner only)
    ChargerResponse createCharger(CreateChargerRequest request);

    ChargerResponse updateCharger(Long id, String name, Double maxPower, ConnectorType connectorType);

    void deleteCharger(Long id);

    // Port management (Owner only)
    PortResponse createPort(CreatePortRequest request);

    PortResponse updatePortStatus(Long id, PortStatus status);

    void deletePort(Long id);
}
