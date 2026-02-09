package com.project.evgo.charger.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.evgo.sharedkernel.enums.PortStatus;

import java.util.List;

/**
 * Repository for Port entity.
 */
public interface PortRepository extends JpaRepository<Port, Long> {

    List<Port> findByChargerId(Long chargerId);

    long countByChargerStationId(Long stationId);

    long countByChargerStationIdAndStatus(Long stationId, PortStatus status);
}
