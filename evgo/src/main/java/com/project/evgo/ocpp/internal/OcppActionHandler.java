package com.project.evgo.ocpp.internal;

import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;

/**
 * Strategy interface for OCPP action handlers.
 * Each implementation handles a specific OCPP action (e.g., BootNotification,
 * Heartbeat).
 */
public interface OcppActionHandler {

    /**
     * @return the OCPP action name this handler supports (e.g., "BootNotification")
     */
    String getAction();

    /**
     * Handle an incoming OCPP CALL for this action.
     *
     * @param chargePointId the identity of the charge point
     * @param call          the parsed OCPP CALL message
     * @return the CallResult to send back
     */
    OcppCallResult handle(String chargePointId, OcppCall call);
}
