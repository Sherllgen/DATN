package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a StopTransaction.req is received from a charge point.
 *
 * @param transactionId the OCPP transaction ID
 * @param meterStop     meter reading in Wh at transaction end
 * @param timestamp     when the transaction ended
 * @param idTag         optional idTag of the user who stopped the transaction
 * @param reason        optional OCPP stop reason (e.g., EmergencyStop, EVDisconnected, etc.)
 */
public record StopTransactionReceivedEvent(
        Integer transactionId,
        Integer meterStop,
        LocalDateTime timestamp,
        String idTag,
        String reason
) {
}
