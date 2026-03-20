package com.project.evgo.charger.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.evgo.sharedkernel.enums.PortStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Port entity.
 */
public interface PortRepository extends JpaRepository<Port, Long> {

    List<Port> findByChargerId(Long chargerId);

    Optional<Port> findByChargerIdAndPortNumber(Long chargerId, Integer portNumber);

    long countByChargerStationId(Long stationId);

    long countByChargerStationIdAndStatus(Long stationId, PortStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT c.connectorType as type, " +
            "COUNT(p) as total, " +
            "COALESCE(SUM(CASE WHEN p.status = 'AVAILABLE' THEN 1 ELSE 0 END), 0) as available " +
            "FROM Charger c LEFT JOIN c.ports p " +
            "WHERE c.stationId = :stationId " +
            "GROUP BY c.connectorType")
    List<PortSummaryProjection> findPortSummariesByStationId(
            @org.springframework.data.repository.query.Param("stationId") Long stationId);
}
