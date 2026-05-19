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

    // If a session is stuck in PREPARING for more than 3 minutes, it will be cleaned up and set to INTERRUPTED
    // The linked booking will be reverted to CONFIRMED so the user can re-attempt charging
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
