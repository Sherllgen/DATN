package com.project.evgo.charging.internal;

import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
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

    /**
     * Paginated query for sessions by userId and status (e.g. COMPLETED history).
     * Ordered by createdAt descending by default via Pageable sort.
     */
    Page<ChargingSession> findByUserIdAndStatus(Long userId, ChargingSessionStatus status, Pageable pageable);

    /**
     * Find the first (most recent) active session for a user among the given statuses.
     * Used by the /me/active endpoint to return 0 or 1 result without fetching all sessions.
     */
    Optional<ChargingSession> findFirstByUserIdAndStatusIn(Long userId, Collection<ChargingSessionStatus> statuses);
}
