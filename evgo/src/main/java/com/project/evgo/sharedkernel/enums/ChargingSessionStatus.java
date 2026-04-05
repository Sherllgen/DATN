package com.project.evgo.sharedkernel.enums;

/**
 * Status of a charging session complying with OCPP states where applicable.
 */
public enum ChargingSessionStatus {
    PREPARING,
    CHARGING,
    SUSPENDED_EV,
    SUSPENDED_EVSE,
    FINISHING,
    COMPLETED,
    FAULTED,
    INTERRUPTED
}
