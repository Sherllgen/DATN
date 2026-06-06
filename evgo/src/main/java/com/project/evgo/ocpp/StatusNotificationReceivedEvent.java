package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a StatusNotification.req is received from a charge point.
 *
 * @param chargePointId   the charge point identity (charger database ID as String)
 * @param connectorId     OCPP connectorId (0 = whole CP, 1+ = specific connector)
 * @param portId          resolved database port ID (null if connectorId is 0)
 * @param errorCode       OCPP ChargePointErrorCode (e.g. "NoError", "GroundFailure")
 * @param status          OCPP ChargePointStatus (e.g. "Available", "Charging", etc.)
 * @param info            optional additional info text (max 50 chars)
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
