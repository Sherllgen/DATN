package com.project.evgo.charging.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for ChargingSession entity.
 */
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    List<ChargingSession> findByUserId(Long userId);
}
