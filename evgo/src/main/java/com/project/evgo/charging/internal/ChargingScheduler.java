package com.project.evgo.charging.internal;

import com.project.evgo.charging.ChargingService;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for charging session maintenance tasks.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChargingScheduler {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingService chargingService;

    // ============================================================
    // Job 1: Clean up PREPARING sessions stuck for > 3 minutes.
    // Reverts the linked booking to CONFIRMED so the user can retry.
    // ============================================================
    @Scheduled(fixedRate = 60000)
    public void cleanupStuckPreparingSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(3);
        List<ChargingSession> stuckSessions = sessionRepository.findByStatusAndCreatedAtBefore(
                ChargingSessionStatus.PREPARING, threshold);

        for (ChargingSession session : stuckSessions) {
            try {
                chargingService.cleanupStuckPreparingSession(session.getId());
            } catch (Exception e) {
                log.error("Failed to cleanup stuck preparing session {}", session.getId(), e);
            }
        }
    }
}
