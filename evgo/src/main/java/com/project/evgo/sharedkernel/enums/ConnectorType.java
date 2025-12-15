package com.project.evgo.sharedkernel.enums;

/**
 * Connector type for EV chargers.
 */
public enum ConnectorType {
    TYPE_1, // J1772 - Common in North America
    TYPE_2, // Mennekes - Common in Europe
    CCS_1, // Combined Charging System Type 1
    CCS_2, // Combined Charging System Type 2
    CHADEMO, // CHAdeMO DC fast charging
    TESLA, // Tesla Supercharger
    GB_T // Chinese standard
}
