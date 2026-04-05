package com.project.evgo.charging;

/**
 * Event published to request a remote stop transaction from OCPP.
 */
public record SendRemoteStopCommandEvent(Long sessionId) {
}
