package com.project.evgo.sharedkernel.events;

import java.time.LocalDateTime;

/**
 * Domain event published when a cable is unplugged after a completed session.
 * Placed in sharedkernel to avoid circular dependencies between charging, payment, and booking.
 * <p>
 * Idle time = {@code cableUnpluggedTime} - {@code idleStartTime}
 *
 * @param sessionId          the charging session database ID
 * @param portId             the port database ID
 * @param userId             the user who owns the session
 * @param idleStartTime      when the session completed (StopTransaction timestamp / endTime)
 * @param cableUnpluggedTime when the cable was physically unplugged
 */
public record CableUnpluggedEvent(
        Long sessionId,
        Long portId,
        Long userId,
        LocalDateTime idleStartTime,
        LocalDateTime cableUnpluggedTime
) {
}
