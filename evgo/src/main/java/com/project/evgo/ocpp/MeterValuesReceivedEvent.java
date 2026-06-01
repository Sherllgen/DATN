package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a MeterValues.req is received from a charge point.
 *
 * @param chargePointId  the charge point identity (charger database ID as String)
 * @param connectorId    OCPP connectorId (0 = main meter, 1+ = specific connector)
 * @param transactionId  optional OCPP transaction ID the meter value belongs to
 * @param meterValue     sampled energy meter value in Wh (Energy.Active.Import.Register)
 * @param timestamp      date/time when the meter sample was taken
 */
public record MeterValuesReceivedEvent(
        String chargePointId,
        Integer connectorId,
        Integer transactionId,
        Integer meterValue,
        LocalDateTime timestamp
) {
}
