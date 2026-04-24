package com.project.evgo.sharedkernel.events;

/**
 * Event published to request a remote stop transaction from OCPP.
 * Placed in sharedkernel to avoid circular dependency: charging ↔ ocpp.
 * <p>
 * Per OCPP 1.6 §6.12, RemoteStopTransaction.req requires {@code transactionId}.
 *
 * @param sessionId     the charging session database ID
 * @param chargePointId the OCPP charge point identity (charger DB ID as String)
 * @param transactionId the active OCPP transaction ID (may be null if session is still PREPARING)
 * @param reason        human-readable reason (e.g., "overstay", "user_requested")
 */
public record SendRemoteStopCommandEvent(
        Long sessionId,
        String chargePointId,
        Integer transactionId,
        String reason
) {
}
