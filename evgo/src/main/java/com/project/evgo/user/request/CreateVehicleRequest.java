package com.project.evgo.user.request;

import com.project.evgo.sharedkernel.enums.ConnectorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Request DTO for adding a new vehicle.
 */
public record CreateVehicleRequest(
    @NotBlank(message = "Brand is required")
    String brand,

    @NotBlank(message = "Model name is required")
    String modelName,

    @NotEmpty(message = "At least one connector type is required")
    Set<ConnectorType> connectorTypes
) {
}
