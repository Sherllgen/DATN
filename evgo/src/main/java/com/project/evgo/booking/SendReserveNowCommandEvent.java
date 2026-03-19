package com.project.evgo.booking;

import java.time.LocalDateTime;

/**
 * Event requesting the OCPP module to send a ReserveNow.req to a charge point.
 * <p>
 * Per OCPP 1.6 §6.11, the payload contains:
 * {@code connectorId}, {@code expiryDate}, {@code idTag}, {@code reservationId}.
 *
 * @param chargePointId the OCPP charge point identity (= charger DB ID as String)
 * @param connectorId   the connector/port number to reserve (0 = any)
 * @param idTag         the RFID/idTag of the user who made the reservation
 * @param expiryDate    when the reservation expires (typically end of the booking block)
 * @param reservationId a unique reservation identifier
 */
public record SendReserveNowCommandEvent(String chargePointId, Integer connectorId,
                                         String idTag, LocalDateTime expiryDate,
                                         Integer reservationId) {
}
