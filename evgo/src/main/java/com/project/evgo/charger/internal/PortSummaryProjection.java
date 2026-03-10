package com.project.evgo.charger.internal;

import com.project.evgo.sharedkernel.enums.ConnectorType;

/**
 * Projection interface for port summaries by connector type.
 */
public interface PortSummaryProjection {
    ConnectorType getType();

    long getAvailable();

    long getTotal();
}
