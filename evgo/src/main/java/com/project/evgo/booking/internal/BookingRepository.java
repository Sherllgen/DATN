package com.project.evgo.booking.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Booking entity.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findBySlotId(Long slotId);
}
