package com.project.evgo.charger.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Port entity.
 */
public interface PortRepository extends JpaRepository<Port, Long> {

    List<Port> findByChargerId(Long chargerId);
}
