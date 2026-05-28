package com.project.evgo.booking.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.evgo.sharedkernel.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Booking entity.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByPortId(Long portId);

    List<Booking> findByStationIdAndStatusInAndStartTimeBetween(
            Long stationId,
            List<BookingStatus> statuses,
            LocalDateTime start,
            LocalDateTime end);

    boolean existsByPortIdAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
            Long portId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<BookingStatus> statuses);

    List<Booking> findByStatusAndStartTimeBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Booking> findByStatusAndEndTimeBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime threshold);

    /**
     * Finds CONFIRMED bookings whose endTime has already passed.
     */
    List<Booking> findByStatusAndEndTimeBefore(BookingStatus status, LocalDateTime cutoff);

    /**
     * Finds no-show CONFIRMED bookings — those whose endTime has already passed
     * AND that have no linked row in the {@code charging_sessions} table.
     */
    @Query(value = """
            SELECT b.* FROM bookings b
            WHERE b.status = 'CONFIRMED'
              AND b.end_time < :cutoff
              AND NOT EXISTS (
                  SELECT 1 FROM charging_sessions cs WHERE cs.booking_id = b.id
              )
            """, nativeQuery = true)
    List<Booking> findExpiredConfirmedBookingsWithNoSession(@Param("cutoff") LocalDateTime cutoff);

    Page<Booking> findByStationIdIn(List<Long> stationIds, Pageable pageable);

    long countByStationIdInAndStatusIn(List<Long> stationIds, List<BookingStatus> statuses);

    @Query("SELECT b.id FROM Booking b WHERE b.stationId IN :stationIds")
    List<Long> findIdsByStationIdIn(@Param("stationIds") List<Long> stationIds);

    @Query("SELECT COUNT(DISTINCT b.userId) FROM Booking b WHERE b.stationId IN :stationIds AND b.status IN :statuses")
    long countDistinctUserIdByStationIdInAndStatusIn(
            @Param("stationIds") List<Long> stationIds,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.status IN :statuses " +
            "AND ((b.startTime >= :startWindowFrom AND b.startTime <= :startWindowTo) " +
            "  OR (b.startTime >= :reminderWindowFrom AND b.startTime <= :reminderWindowTo) " +
            "  OR (b.endTime >= :endWindowFrom AND b.endTime <= :endWindowTo) " +
            "  OR (b.endTime >= :startWindowFrom AND b.endTime <= :startWindowTo))")
    List<Booking> findBookingsNeedingAction(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("startWindowFrom") LocalDateTime startWindowFrom,
            @Param("startWindowTo") LocalDateTime startWindowTo,
            @Param("reminderWindowFrom") LocalDateTime reminderWindowFrom,
            @Param("reminderWindowTo") LocalDateTime reminderWindowTo,
            @Param("endWindowFrom") LocalDateTime endWindowFrom,
            @Param("endWindowTo") LocalDateTime endWindowTo);

    @Query("SELECT MONTH(b.createdAt) as month, COUNT(b) as cnt " +
            "FROM Booking b " +
            "WHERE b.stationId IN :stationIds " +
            "AND b.status = :status " +
            "AND YEAR(b.createdAt) = :year " +
            "GROUP BY MONTH(b.createdAt)")
    List<Object[]> countMonthlyByStationIdsAndStatusAndYear(
            @Param("stationIds") List<Long> stationIds,
            @Param("status") BookingStatus status,
            @Param("year") int year);


    @Query("SELECT DISTINCT b.portId FROM Booking b WHERE b.portId IN :portIds AND b.status = :status AND b.startTime > :after")
    List<Long> findPortIdsWithUpcomingBookings(
            @Param("portIds") List<Long> portIds,
            @Param("status") BookingStatus status,
            @Param("after") LocalDateTime after);
}
