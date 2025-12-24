package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.ConnectorType;

import java.util.Set;

/**
 * DTO representing a vehicle catalog entry.
 * Used to provide predefined vehicle options to users.
 */
public record VehicleCatalogEntry(
    String brand,
    String model,
    Set<ConnectorType> connectorTypes
) {
}
