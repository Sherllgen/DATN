package com.project.evgo.charger.request;

import com.project.evgo.sharedkernel.enums.ConnectorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for updating an existing charger.
 */
public record UpdateChargerRequest(
    @NotBlank(message = "Charger name is required")
    String name,

    @NotNull(message = "Max power is required")
    @Positive(message = "Max power must be positive")
    Double maxPower,

    @NotNull(message = "Connector type is required")
    ConnectorType connectorType) {
}
