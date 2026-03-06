package com.project.evgo.charger.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.ChargerStatistic;
import com.project.evgo.charger.request.CreateChargerRequest;
import com.project.evgo.charger.request.CreatePortRequest;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.PortStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.StationService;
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
    private final StationService stationService;
    private final ChargerDtoConverter converter;

    // ==================== READ OPERATIONS ====================

    // ==================== Charger ====================
    @Override
    public List<ChargerResponse> findByStationId(Long stationId) {
        return converter.toChargerResponse(chargerRepository.findByStationId(stationId));
    }

    @Override
    public Optional<ChargerResponse> findById(Long id) {
        return converter.toChargerResponse(chargerRepository.findById(id));
    }

    // ==================== Port ====================
    @Override
    public List<PortResponse> findPortsByChargerId(Long chargerId) {
        return converter.toPortResponse(portRepository.findByChargerId(chargerId));
    }

    @Override
    public Optional<PortResponse> findPortById(Long id) {
        return converter.toPortResponse(portRepository.findById(id));
    }

    // ==================== CHARGER MANAGEMENT ====================

    @Override
    @Transactional
    public ChargerResponse createCharger(CreateChargerRequest request) {
        // Verify ownership via StationService public API
        stationService.verifyOwnership(request.getStationId());

        Charger charger = new Charger();
        charger.setName(request.getName());
        charger.setMaxPower(request.getMaxPower());
        charger.setConnectorType(ConnectorType.VINFAST_STD); // Default, TODO: add to request
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStationId(request.getStationId());

        Charger saved = chargerRepository.save(charger);
        return converter.toChargerResponse(saved);
    }

    @Override
    @Transactional
    public ChargerResponse updateCharger(Long id, String name, Double maxPower, ConnectorType connectorType) {
        Charger charger = findChargerAndVerifyOwner(id);

        charger.setName(name);
        charger.setMaxPower(maxPower);
        charger.setConnectorType(connectorType);

        Charger saved = chargerRepository.save(charger);
        return converter.toChargerResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCharger(Long id) {
        Charger charger = findChargerAndVerifyOwner(id);
        chargerRepository.delete(charger);
    }

    // ==================== PORT MANAGEMENT ====================

    @Override
    @Transactional
    public PortResponse createPort(Long chargerId, CreatePortRequest request) {
        Charger charger = findChargerAndVerifyOwner(chargerId);

        Port port = new Port();
        port.setPortNumber(request.portNumber());
        port.setStatus(PortStatus.AVAILABLE);
        port.setCharger(charger);

        Port saved = portRepository.save(port);
        return converter.toPortResponse(saved);
    }

    @Override
    @Transactional
    public PortResponse updatePortStatus(Long id, PortStatus status) {
        Port port = findPortAndVerifyOwner(id);

        port.setStatus(status);

        Port saved = portRepository.save(port);
        return converter.toPortResponse(saved);
    }

    @Override
    @Transactional
    public void deletePort(Long id) {
        Port port = findPortAndVerifyOwner(id);
        portRepository.delete(port);
    }

    // ==================== HELPER METHODS ====================

    private Charger findChargerAndVerifyOwner(Long chargerId) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new AppException(ErrorCode.CHARGER_NOT_FOUND));

        // Verify ownership via StationService public API
        try {
            stationService.verifyOwnership(charger.getStationId());
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.STATION_NOT_OWNED) {
                throw new AppException(ErrorCode.CHARGER_NOT_OWNED);
            }
            throw e;
        }

        return charger;
    }

    private Port findPortAndVerifyOwner(Long portId) {
        Port port = portRepository.findById(portId)
                .orElseThrow(() -> new AppException(ErrorCode.PORT_NOT_FOUND));

        Charger charger = port.getCharger();

        // Verify ownership via StationService public API
        try {
            stationService.verifyOwnership(charger.getStationId());
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.STATION_NOT_OWNED) {
                throw new AppException(ErrorCode.CHARGER_NOT_OWNED);
            }
            throw e;
        }

        return port;
    }

    @Override
    public List<ChargerStatistic> findStatisticsByStationId(Long stationId) {
        return chargerRepository.findStatisticsByStationId(stationId).stream()
                .map(p -> new ChargerStatistic(p.getType(), p.getStatus(), p.getCount()))
                .toList();
    }
}
