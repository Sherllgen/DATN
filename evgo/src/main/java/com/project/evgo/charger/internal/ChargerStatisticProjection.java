package com.project.evgo.charger.internal;

import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;

/**
 * Projection interface for charger statistics.
 */
public interface ChargerStatisticProjection {
    ConnectorType getType();

    ChargerStatus getStatus();

    long getCount();
}
