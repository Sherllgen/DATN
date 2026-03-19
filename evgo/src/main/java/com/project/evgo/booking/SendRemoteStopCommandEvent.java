package com.project.evgo.booking;

/**
 * Event requesting the OCPP module to send a RemoteStopTransaction.req to a charge point.
 * <p>
 * Per OCPP 1.6 §6.12, the payload contains a single field: {@code transactionId}.
 *
 * @param chargePointId the OCPP charge point identity (= charger DB ID as String)
 * @param transactionId the active transaction ID to stop 
 * @param reason        human-readable reason (e.g. "overstay", "hard-cutoff")
 */
public record SendRemoteStopCommandEvent(String chargePointId, Integer transactionId, String reason) {
}
