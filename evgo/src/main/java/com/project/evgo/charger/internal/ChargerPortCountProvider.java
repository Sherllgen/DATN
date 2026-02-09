package com.project.evgo.charger.internal;

import com.project.evgo.sharedkernel.enums.PortStatus;
import com.project.evgo.station.PortCountProvider;
import com.project.evgo.station.PortCounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementation of PortCountProvider.
 */
@Component
@RequiredArgsConstructor
class ChargerPortCountProvider implements PortCountProvider {

    private final PortRepository portRepository;

    @Override
    public PortCounts getPortCounts(Long stationId) {
        long totalPorts = portRepository.countByChargerStationId(stationId);
        long availablePorts = portRepository.countByChargerStationIdAndStatus(stationId, PortStatus.AVAILABLE);
        return new PortCounts((int) totalPorts, (int) availablePorts);
    }
}
