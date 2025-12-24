package com.project.evgo.station.internal;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Station entity.
 * Internal - not accessible by other modules.
 */
public interface StationRepository extends JpaRepository<Station, Long> {
}
