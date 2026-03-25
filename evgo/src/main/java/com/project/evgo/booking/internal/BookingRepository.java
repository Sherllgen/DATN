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

    List<Booking> findByStationIdAndPortNumber(Long stationId, Integer portNumber);

    List<Booking> findByStationIdAndStatusInAndStartTimeBetween(
            Long stationId,
            List<BookingStatus> statuses,
            LocalDateTime start,
            LocalDateTime end);

    boolean existsByStationIdAndPortNumberAndEndTimeAfterAndStartTimeBeforeAndStatusIn(
            Long stationId,
            Integer portNumber,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<BookingStatus> statuses);

    List<Booking> findByStatusAndStartTimeBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Booking> findByStatusAndEndTimeBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime threshold);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.status IN :statuses " +
            "AND ((b.startTime >= :startWindowFrom AND b.startTime <= :startWindowTo) " +
            "  OR (b.endTime >= :endWindowFrom AND b.endTime <= :endWindowTo) " +
            "  OR (b.endTime >= :startWindowFrom AND b.endTime <= :startWindowTo))")
    List<Booking> findBookingsNeedingAction(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("startWindowFrom") LocalDateTime startWindowFrom,
            @Param("startWindowTo") LocalDateTime startWindowTo,
            @Param("endWindowFrom") LocalDateTime endWindowFrom,
            @Param("endWindowTo") LocalDateTime endWindowTo);
}
