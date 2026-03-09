package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;

public record StationChargerStatistic(ConnectorType type, ChargerStatus status, long count) {
}
