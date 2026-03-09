package com.project.evgo.charger;

import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;

/**
 * Public DTO for charger statistics — exposed by charger module.
 */
public record ChargerStatistic(
        ConnectorType type,
        ChargerStatus status,
        long count) {
}
