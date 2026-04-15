package com.project.evgo.sharedkernel.events;

/**
 * Event published to request a remote start transaction from OCPP.
 * Placed in sharedkernel to avoid circular dependency: charging ↔ ocpp.
 * <p>
 * Per OCPP 1.6 §6.5, RemoteStartTransaction.req requires {@code idTag}
 * and optionally {@code connectorId}.
 *
 * @param sessionId     the charging session database ID
 * @param chargePointId the OCPP charge point identity (charger DB ID as String)
 * @param connectorId   the OCPP connectorId (= port number on the charger)
 * @param idTag         identifier used to authorize the transaction (userId as String)
 */
public record SendRemoteStartCommandEvent(
        Long sessionId,
        String chargePointId,
        Integer connectorId,
        String idTag
) {
}
