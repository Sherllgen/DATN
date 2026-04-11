package com.project.evgo.sharedkernel.enums;

/**
 * Lifecycle status of a charging session.
 * <p>
 * Maps to OCPP 1.6 ChargePointStatus where applicable, with additional
 * domain-specific terminal states for the session lifecycle.
 * <p>
 * <b>OCPP 1.6 §5 — Typical connector status flow:</b>
 * <pre>Available → Preparing → Charging → SuspendedEV/SuspendedEVSE → Finishing → Available</pre>
 * <p>
 * <b>Charging session lifecycle:</b>
 * <pre>PREPARING → CHARGING → (SUSPENDED_EV|SUSPENDED_EVSE) → FINISHING → COMPLETED</pre>
 */
public enum ChargingSessionStatus {

    /** Connector is preparing; waiting for StartTransaction from the charge point. */
    PREPARING,

    /** Energy transfer is active (StartTransaction received). */
    CHARGING,

    /** Vehicle paused charging (e.g. battery full, user paused from vehicle). */
    SUSPENDED_EV,

    /** EVSE paused charging (e.g. smart charging, power limit). */
    SUSPENDED_EVSE,

    /**
     * Transaction ended (StopTransaction received), cable may still be connected.
     * Idle fee calculation starts from this point.
     */
    FINISHING,

    /** Hardware error reported by the charge point. */
    FAULTED,

    // ── Domain-specific states (not defined in OCPP 1.6) ──

    /**
     * Session fully closed — cable unplugged (connector returned to Available).
     * Idle fee calculation ends at this point.
     */
    COMPLETED,

    /** Session cancelled by user or system before StartTransaction was received. */
    INTERRUPTED
}
