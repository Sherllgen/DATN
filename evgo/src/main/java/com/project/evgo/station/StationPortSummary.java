package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.ConnectorType;

/**
 * Public DTO for port summaries by connector type — used by station module.
 */
public record StationPortSummary(
        ConnectorType type,
        int available,
        int total) {
}
