package com.project.evgo.charging.internal;

import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChargingSession entity.
 */
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    List<ChargingSession> findByUserId(Long userId);
    
    Optional<ChargingSession> findByTransactionId(Integer transactionId);
    
    Optional<ChargingSession> findByBookingId(Long bookingId);

    Optional<ChargingSession> findByPortIdAndStatus(Long portId, ChargingSessionStatus status);

    boolean existsByPortIdAndStatusIn(Long portId, List<ChargingSessionStatus> statuses);

    Optional<ChargingSession> findFirstByPortIdAndStatusIn(Long portId, List<ChargingSessionStatus> statuses);
}
