package com.project.evgo.charger;

import com.project.evgo.charger.request.CreateChargerRequest;
import com.project.evgo.charger.request.CreatePortRequest;
import com.project.evgo.charger.request.UpdateChargerRequest;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
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

    Optional<PortResponse> findPortByChargerIdAndPortNumber(Long chargerId, Integer portNumber);

    Optional<PortResponse> findPortById(Long id);

    // Charger management (Owner only)
    ChargerResponse createCharger(CreateChargerRequest request);

    ChargerResponse updateCharger(Long id, UpdateChargerRequest request);

    void deleteCharger(Long id);

    // Port management (Owner only)
    PortResponse createPort(Long chargerId, CreatePortRequest request);

    PortResponse updatePortStatus(Long id, PortStatus status);

    void deletePort(Long id);

    long countByStationId(Long stationId);

    long countAvailableByStationId(Long stationId);

    // -------------------------------------OCPP
    // operations-------------------------------------

    /**
     * Process a BootNotification from an OCPP charge point.
     * Updates charger metadata and status, publishes ChargePointBootedEvent.
     *
     * @param chargerId the database ID of the charger
     * @return the updated ChargerResponse, or empty if charger not found
     */
    Optional<ChargerResponse> processBootNotification(
            Long chargerId, String vendor, String model, String serial, String firmware);

    /**
     * Update the last heartbeat timestamp for a charge point.
     *
     * @param chargerId the database ID of the charger
     */
    void updateHeartbeat(Long chargerId);

    void internalUpdatePortStatus(Long portId, PortStatus status);
}
