package com.project.evgo.sharedkernel.enums;

/**
 * Status of a charger, aligned with OCPP 1.6 ChargePointStatus.
 */
public enum ChargerStatus {
    AVAILABLE, // OCPP: Available — ready for new transaction
    PREPARING, // OCPP: Preparing — connector plugged, not yet charging
    CHARGING, // OCPP: Charging — actively delivering energy
    SUSPENDED_EVSE, // OCPP: SuspendedEVSE — charger limits/pauses power
    SUSPENDED_EV, // OCPP: SuspendedEV — vehicle limits/pauses power
    FINISHING, // OCPP: Finishing — transaction ended, cable still connected
    RESERVED, // OCPP: Reserved — reserved for specific user
    UNAVAILABLE, // OCPP: Unavailable — maintenance or admin-disabled
    FAULTED, // OCPP: Faulted — hardware error
    OFFLINE // Internal: no active WebSocket connection
}
