package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a StartTransaction.req is received from a charge point.
 *
 * @param chargePointId  the charge point identity (charger database ID as String)
 * @param connectorId    OCPP connectorId (1-indexed)
 * @param portId         resolved database port ID from chargePointId + connectorId
 * @param transactionId  transaction ID assigned by Central System
 * @param idTag          the idTag that authorized the transaction (max 20 chars)
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
