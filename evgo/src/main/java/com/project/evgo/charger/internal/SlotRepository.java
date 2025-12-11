package com.project.evgo.charger.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Slot entity.
 */
public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByChargerId(Long chargerId);
}
