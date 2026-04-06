package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a StopTransaction.req is received from a charge point.
 * <p>
 * Per OCPP 1.6 §4.12 (StopTransaction):
 * <ul>
 *   <li>{@code transactionId} — the OCPP transaction ID (required)</li>
 *   <li>{@code meterStop} — meter value in Wh at end of transaction (required)</li>
 *   <li>{@code timestamp} — date/time when the transaction ended (required)</li>
 *   <li>{@code idTag} — optional identifier of the user who requested the stop</li>
 *   <li>{@code reason} — optional stop reason as per OCPP spec</li>
 * </ul>
 * Valid reasons: EmergencyStop, EVDisconnected, HardReset, Local, Other,
 * PowerLoss, Reboot, Remote, SoftReset, UnlockCommand, DeAuthorized
 *
 * @param transactionId the OCPP transaction ID
 * @param meterStop     meter reading in Wh at transaction end
 * @param timestamp     when the transaction ended
 * @param idTag         optional idTag of the user who stopped the transaction
 * @param reason        optional OCPP stop reason
 */
public record StopTransactionReceivedEvent(
        Integer transactionId,
        Integer meterStop,
        LocalDateTime timestamp,
        String idTag,
        String reason
) {
}
