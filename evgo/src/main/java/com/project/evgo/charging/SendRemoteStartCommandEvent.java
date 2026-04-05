package com.project.evgo.charging;

/**
 * Event published to request a remote start transaction from OCPP.
 */
public record SendRemoteStartCommandEvent(Long sessionId) {
}
