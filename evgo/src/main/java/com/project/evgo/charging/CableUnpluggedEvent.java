package com.project.evgo.charging;

/**
 * Event published when a cable is unplugged after a completed session.
 * Crucial for stopping idle fee calculation.
 *
 * @param sessionId the charging session database ID
 * @param portId    the port database ID
 */
public record CableUnpluggedEvent(Long sessionId, Long portId) {
}
