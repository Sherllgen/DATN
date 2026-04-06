package com.project.evgo.charging;

import java.math.BigDecimal;

/**
 * Event published when a charging session is completed (StopTransaction received).
 *
 * @param sessionId the charging session database ID
 * @param userId    the user who owns the session
 * @param portId    the port database ID
 * @param totalKwh  total energy consumed in kWh
 */
public record ChargingSessionCompletedEvent(Long sessionId, Long userId, Long portId, BigDecimal totalKwh) {
}
