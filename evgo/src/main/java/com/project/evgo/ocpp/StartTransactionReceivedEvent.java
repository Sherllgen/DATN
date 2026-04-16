package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a StartTransaction.req is received from a charge point.
 * <p>
 * Per OCPP 1.6 §4.10 (StartTransaction):
 * <ul>
 *   <li>{@code connectorId} — 1-indexed connector on the charge point</li>
 *   <li>{@code idTag} — identifier used to authorize the transaction (max 20 chars)</li>
 *   <li>{@code meterStart} — meter value in Wh at start of transaction</li>
 *   <li>{@code timestamp} — date/time when the transaction started</li>
 *   <li>{@code reservationId} — optional reservation that preceded this transaction</li>
 * </ul>
 * Additional fields resolved by the OCPP module before publishing:
 * <ul>
 *   <li>{@code chargePointId} — the OCPP charge point identity (= charger DB ID)</li>
 *   <li>{@code portId} — resolved database port ID from chargePointId + connectorId</li>
 *   <li>{@code transactionId} — assigned by the Central System in StartTransaction.conf</li>
 * </ul>
 *
 * @param chargePointId  the charge point identity (charger database ID as String)
 * @param connectorId    OCPP connectorId (1-indexed)
 * @param portId         resolved database port ID
 * @param transactionId  transaction ID assigned by Central System
 * @param idTag          the idTag that authorized the transaction
 * @param meterStart     meter reading in Wh at transaction start
 * @param timestamp      when the transaction started
 * @param reservationId  optional reservation ID (null if none)
 */
public record StartTransactionReceivedEvent(
        String chargePointId,
        Integer connectorId,
        Long portId,
        Integer transactionId,
        String idTag,
        Integer meterStart,
        LocalDateTime timestamp,
        Integer reservationId
) {
}
