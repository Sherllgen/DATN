package com.project.evgo.charger.response;

import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for charger information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargerResponse {

    private Long id;
    private String name;
    private Double maxPower;
    private ConnectorType connectorType;
    private ChargerStatus status;
    private Long stationId;
    private List<PortResponse> ports;
    private Integer totalPorts;
    private Integer availablePorts;
    private LocalDateTime createdAt;
}
