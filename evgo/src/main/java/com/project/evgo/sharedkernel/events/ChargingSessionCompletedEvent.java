package com.project.evgo.sharedkernel.events;

import java.math.BigDecimal;

/**
 * Domain event published when a charging session is completed.
 * Placed in sharedkernel to avoid circular dependencies between charging, payment, and booking.
 *
 * @param sessionId the charging session database ID
 * @param userId    the user who owns the session
 * @param portId    the port database ID
 * @param totalKwh  total energy consumed in kWh
 */
public record ChargingSessionCompletedEvent(
    Long sessionId, 
    Long userId, 
    Long portId, 
    BigDecimal totalKwh
) {
}
