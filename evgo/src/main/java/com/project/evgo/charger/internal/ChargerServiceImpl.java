package com.project.evgo.charger.internal;

import com.project.evgo.charger.ChargePointBootedEvent;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.request.CreateChargerRequest;
import com.project.evgo.charger.request.CreatePortRequest;
import com.project.evgo.charger.request.UpdateChargerRequest;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.PortStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.StationOwnershipValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ChargerService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargerServiceImpl implements ChargerService {

    private final ChargerRepository chargerRepository;
    private final PortRepository portRepository;
    private final StationOwnershipValidator stationValidator;
    private final ChargerDtoConverter converter;
    private final ApplicationEventPublisher eventPublisher;

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

    @Override
    public Optional<PortResponse> findPortByChargerIdAndPortNumber(Long chargerId, Integer portNumber) {
        return converter.toPortResponse(portRepository.findByChargerIdAndPortNumber(chargerId, portNumber));
    }

    // ==================== CHARGER MANAGEMENT ====================

    @Override
    @Transactional
    public ChargerResponse createCharger(CreateChargerRequest request) {
        // Verify ownership via StationOwnershipValidator public API
        stationValidator.verifyOwnership(request.getStationId());

        Charger charger = new Charger();
        charger.setName(request.getName());
        charger.setMaxPower(request.getMaxPower());
        charger.setConnectorType(request.getConnectorType());
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStationId(request.getStationId());

        Charger saved = chargerRepository.save(charger);
        return converter.toChargerResponse(saved);
    }

    @Override
    @Transactional
    public ChargerResponse updateCharger(Long id, UpdateChargerRequest request) {
        Charger charger = findChargerAndVerifyOwner(id);

        charger.setName(request.name());
        charger.setMaxPower(request.maxPower());
        charger.setConnectorType(request.connectorType());

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

        // Verify ownership via StationOwnershipValidator public API
        try {
            stationValidator.verifyOwnership(charger.getStationId());
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

        // Verify ownership via StationOwnershipValidator public API
        try {
            stationValidator.verifyOwnership(charger.getStationId());
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.STATION_NOT_OWNED) {
                throw new AppException(ErrorCode.CHARGER_NOT_OWNED);
            }
            throw e;
        }

        return port;
    }

    @Override
    public long countByStationId(Long stationId) {
        return chargerRepository.countByStationId(stationId);
    }

    @Override
    public long countAvailableByStationId(Long stationId) {
        return chargerRepository.countByStationIdAndStatus(stationId, ChargerStatus.AVAILABLE);
    }

    // ==================== OCPP OPERATIONS ====================

    @Override
    @Transactional
    public Optional<ChargerResponse> processBootNotification(
            Long chargerId, String vendor, String model, String serial, String firmware) {

        Optional<Charger> optionalCharger = chargerRepository.findById(chargerId);
        if (optionalCharger.isEmpty()) {
            log.warn("BootNotification from unregistered charge point ID: {}", chargerId);
            return Optional.empty();
        }

        Charger charger = optionalCharger.get();
        charger.setChargePointVendor(vendor);
        charger.setChargePointModel(model);
        charger.setChargePointSerial(serial);
        charger.setFirmwareVersion(firmware);
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setLastHeartbeat(Instant.now());

        Charger saved = chargerRepository.save(charger);
        log.info("BootNotification processed for charge point ID: {}", saved.getId());

        eventPublisher.publishEvent(new ChargePointBootedEvent(saved.getId()));

        return Optional.of(converter.toChargerResponse(saved));
    }

    @Override
    @Transactional
    public void updateHeartbeat(Long chargerId) {
        chargerRepository.findById(chargerId)
                .ifPresent(charger -> {
                    charger.setLastHeartbeat(Instant.now());
                    chargerRepository.save(charger);
                    log.debug("Heartbeat updated for charge point ID: {}", chargerId);
                });
    }

    @Override
    @Transactional
    public void internalUpdatePortStatus(Long portId, PortStatus status) {
        portRepository.findById(portId).ifPresent(port -> {
            port.setStatus(status);
            portRepository.save(port);
            log.debug("Internal port status update for port ID: {} to {}", portId, status);
        });
    }
}
