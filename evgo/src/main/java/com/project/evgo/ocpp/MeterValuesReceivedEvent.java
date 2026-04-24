package com.project.evgo.ocpp;

import java.time.LocalDateTime;

/**
 * Event published by the OCPP module when a MeterValues.req is received from a charge point.
 * <p>
 * Per OCPP 1.6 §4.4 (MeterValues):
 * <ul>
 *   <li>{@code connectorId} — 0-indexed connector (0 = main meter, 1+ = specific connector)</li>
 *   <li>{@code transactionId} — optional transaction ID the meter value belongs to</li>
 *   <li>{@code meterValue} — the sampled meter value in Wh (Energy.Active.Import.Register)</li>
 *   <li>{@code timestamp} — date/time of the meter sample</li>
 * </ul>
 * Additional fields resolved by the OCPP module before publishing:
 * <ul>
 *   <li>{@code chargePointId} — the OCPP charge point identity</li>
 * </ul>
 *
 * @param chargePointId  the charge point identity (charger database ID as String)
 * @param connectorId    OCPP connectorId (0 = main meter, 1+ = specific connector)
 * @param transactionId  optional OCPP transaction ID
 * @param meterValue     sampled energy meter value in Wh (Energy.Active.Import.Register)
 * @param timestamp      when the meter sample was taken
 */
public record MeterValuesReceivedEvent(
        String chargePointId,
        Integer connectorId,
        Integer transactionId,
        Integer meterValue,
        LocalDateTime timestamp
) {
}
