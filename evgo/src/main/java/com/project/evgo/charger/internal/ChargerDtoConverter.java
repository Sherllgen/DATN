package com.project.evgo.charger.internal;

import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.PortStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Charger and Port entities to DTOs.
 */
@Component
public class ChargerDtoConverter {

    public ChargerResponse toChargerResponse(Charger charger) {
        List<PortResponse> portResponses = charger.getPorts().stream()
                .map(this::toPortResponse)
                .toList();

        int totalPorts = portResponses.size();
        int availablePorts = (int) charger.getPorts().stream()
                .filter(p -> p.getStatus() == PortStatus.AVAILABLE)
                .count();

        return ChargerResponse.builder()
                .id(charger.getId())
                .name(charger.getName())
                .maxPower(charger.getMaxPower())
                .connectorType(charger.getConnectorType())
                .status(charger.getStatus())
                .stationId(charger.getStationId())
                .ports(portResponses)
                .totalPorts(totalPorts)
                .availablePorts(availablePorts)
                .createdAt(charger.getCreatedAt())
                .build();
    }

    public PortResponse toPortResponse(Port port) {
        return PortResponse.builder()
                .id(port.getId())
                .portNumber(port.getPortNumber())
                .status(port.getStatus())
                .chargerId(port.getCharger().getId())
                .createdAt(port.getCreatedAt())
                .build();
    }

    public List<ChargerResponse> toChargerResponse(List<Charger> chargers) {
        return chargers.stream()
                .map(this::toChargerResponse)
                .toList();
    }

    public List<PortResponse> toPortResponse(List<Port> ports) {
        return ports.stream()
                .map(this::toPortResponse)
                .toList();
    }

    public Optional<ChargerResponse> toChargerResponse(Optional<Charger> charger) {
        return charger.map(this::toChargerResponse);
    }

    public Optional<PortResponse> toPortResponse(Optional<Port> port) {
        return port.map(this::toPortResponse);
    }
}
