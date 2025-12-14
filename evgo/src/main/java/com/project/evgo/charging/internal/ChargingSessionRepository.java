package com.project.evgo.charging.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChargingSession entity.
 */
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    Optional<ChargingSession> findByBookingId(Long bookingId);

    List<ChargingSession> findByUserId(Long userId);
}
