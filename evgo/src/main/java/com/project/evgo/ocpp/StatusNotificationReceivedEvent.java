package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a StatusNotification.req is received from a charge point.
 * <p>
 * Per OCPP 1.6 §4.11 (StatusNotification):
 * <ul>
 *   <li>{@code connectorId} — 1-indexed connector; 0 = entire charge point (required)</li>
 *   <li>{@code errorCode} — ChargePointErrorCode enum value (required)</li>
 *   <li>{@code status} — ChargePointStatus enum value (required):
 *        Available, Preparing, Charging, SuspendedEVSE, SuspendedEV,
 *        Finishing, Reserved, Unavailable, Faulted</li>
 *   <li>{@code info} — additional free-text info (optional, max 50 chars)</li>
 *   <li>{@code timestamp} — when the status change occurred (optional)</li>
 *   <li>{@code vendorErrorCode} — vendor-specific error code (optional)</li>
 *   <li>{@code vendorId} — vendor identifier (optional)</li>
 * </ul>
 * Additional fields resolved by the OCPP module:
 * <ul>
 *   <li>{@code chargePointId} — the OCPP charge point identity (= charger DB ID)</li>
 *   <li>{@code portId} — resolved database port ID (null if connectorId=0)</li>
 * </ul>
 *
 * @param chargePointId   the charge point identity (charger database ID as String)
 * @param connectorId     OCPP connectorId (0 = whole CP, 1+ = specific connector)
 * @param portId          resolved database port ID (null if connectorId is 0)
 * @param errorCode       OCPP ChargePointErrorCode (e.g. "NoError", "GroundFailure")
 * @param status          OCPP ChargePointStatus (e.g. "Available", "Charging")
 * @param info            optional additional info text
 * @param timestamp       optional timestamp of the status change
 * @param vendorErrorCode optional vendor-specific error code
 * @param vendorId        optional vendor identifier
 */
public record StatusNotificationReceivedEvent(
        String chargePointId,
        Integer connectorId,
        Long portId,
        String errorCode,
        String status,
        String info,
        LocalDateTime timestamp,
        String vendorErrorCode,
        String vendorId
) {
}
