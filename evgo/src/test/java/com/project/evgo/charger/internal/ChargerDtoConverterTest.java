package com.project.evgo.charger.internal;

import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.sharedkernel.enums.PortStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ChargerDtoConverterTest {

    private ChargerDtoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ChargerDtoConverter();
    }

    @Test
    void toChargerResponse_shouldMapAllFieldsAndCountPortsCorrectly() {
        // Arrange
        Charger charger = new Charger();
        charger.setId(10L);
        charger.setName("Charger 1");
        charger.setMaxPower(50.0);
        charger.setConnectorType(ConnectorType.IEC_TYPE_2);
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStationId(100L);
        charger.setChargePointVendor("VendorA");
        charger.setChargePointModel("ModelX");
        charger.setChargePointSerial("SN123");
        charger.setFirmwareVersion("v1.0");
        charger.setLastHeartbeat(Instant.ofEpochSecond(1600000000L));
        charger.setCreatedAt(LocalDateTime.of(2023, Month.JANUARY, 1, 10, 0));

        Port port1 = new Port();
        port1.setId(1L);
        port1.setPortNumber(1);
        port1.setStatus(PortStatus.AVAILABLE);
        port1.setCharger(charger);

        Port port2 = new Port();
        port2.setId(2L);
        port2.setPortNumber(2);
        port2.setStatus(PortStatus.CHARGING);
        port2.setCharger(charger);

        charger.setPorts(List.of(port1, port2));

        // Act
        ChargerResponse response = converter.toChargerResponse(charger);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Charger 1");
        assertThat(response.getMaxPower()).isEqualTo(50.0);
        assertThat(response.getConnectorType()).isEqualTo(ConnectorType.IEC_TYPE_2);
        assertThat(response.getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
        assertThat(response.getStationId()).isEqualTo(100L);
        assertThat(response.getChargePointVendor()).isEqualTo("VendorA");
        assertThat(response.getChargePointModel()).isEqualTo("ModelX");
        assertThat(response.getChargePointSerial()).isEqualTo("SN123");
        assertThat(response.getFirmwareVersion()).isEqualTo("v1.0");
        assertThat(response.getLastHeartbeat()).isEqualTo(Instant.ofEpochSecond(1600000000L));
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2023, Month.JANUARY, 1, 10, 0));

        assertThat(response.getTotalPorts()).isEqualTo(2);
        assertThat(response.getAvailablePorts()).isEqualTo(1);
        assertThat(response.getPorts()).hasSize(2);
    }

    @Test
    void toPortResponse_shouldMapAllFieldsCorrectly() {
        // Arrange
        Charger charger = new Charger();
        charger.setId(20L);

        Port port = new Port();
        port.setId(2L);
        port.setPortNumber(3);
        port.setStatus(PortStatus.FAULTED);
        port.setCharger(charger);
        port.setCreatedAt(LocalDateTime.of(2023, Month.FEBRUARY, 1, 12, 0));

        // Act
        PortResponse response = converter.toPortResponse(port);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getPortNumber()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(PortStatus.FAULTED);
        assertThat(response.getChargerId()).isEqualTo(20L);
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2023, Month.FEBRUARY, 1, 12, 0));
    }

    @Test
    void toChargerResponseList_shouldMapAllElements() {
        // Arrange
        Charger charger1 = new Charger();
        charger1.setId(1L);
        charger1.setStatus(ChargerStatus.AVAILABLE);

        Charger charger2 = new Charger();
        charger2.setId(2L);
        charger2.setStatus(ChargerStatus.FAULTED);

        // Act
        List<ChargerResponse> responses = converter.toChargerResponse(List.of(charger1, charger2));

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toPortResponseList_shouldMapAllElements() {
        // Arrange
        Charger charger = new Charger();
        charger.setId(10L);

        Port port1 = new Port();
        port1.setId(1L);
        port1.setCharger(charger);

        Port port2 = new Port();
        port2.setId(2L);
        port2.setCharger(charger);

        // Act
        List<PortResponse> responses = converter.toPortResponse(List.of(port1, port2));

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toChargerResponseOptional_shouldMapIfPresent() {
        // Arrange
        Charger charger = new Charger();
        charger.setId(1L);

        // Act
        Optional<ChargerResponse> response = converter.toChargerResponse(Optional.of(charger));

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(1L);
    }

    @Test
    void toChargerResponseOptional_shouldReturnEmptyIfEmpty() {
        // Act
        Optional<ChargerResponse> response = converter.toChargerResponse(Optional.empty());

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    void toPortResponseOptional_shouldMapIfPresent() {
        // Arrange
        Charger charger = new Charger();
        charger.setId(1L);

        Port port = new Port();
        port.setId(1L);
        port.setCharger(charger);

        // Act
        Optional<PortResponse> response = converter.toPortResponse(Optional.of(port));

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(1L);
    }

    @Test
    void toPortResponseOptional_shouldReturnEmptyIfEmpty() {
        // Act
        Optional<PortResponse> response = converter.toPortResponse(Optional.empty());

        // Assert
        assertThat(response).isEmpty();
    }
}
