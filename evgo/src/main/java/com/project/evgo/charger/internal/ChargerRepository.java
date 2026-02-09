package com.project.evgo.charger.internal;

import com.project.evgo.sharedkernel.enums.ChargerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for Charger entity.
 */
public interface ChargerRepository extends JpaRepository<Charger, Long> {

    List<Charger> findByStationId(Long stationId);

    long countByStationId(Long stationId);

    long countByStationIdAndStatus(Long stationId, ChargerStatus status);

    @Query("SELECT c.connectorType as type, c.status as status, COUNT(c) as count "
            +
            "FROM Charger c WHERE c.stationId = :stationId " +
            "GROUP BY c.connectorType, c.status")
    List<ChargerStatisticProjection> findStatisticsByStationId(Long stationId);
}
