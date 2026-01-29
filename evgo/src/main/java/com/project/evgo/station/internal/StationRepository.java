package com.project.evgo.station.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Station entity.
 * Internal - not accessible by other modules.
 */
public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findByOwnerIdAndDeletedAtIsNull(Long ownerId);

    boolean existsByNameAndOwnerIdAndDeletedAtIsNull(String name, Long ownerId);

    boolean existsByNameAndOwnerIdAndIdNotAndDeletedAtIsNull(String name, Long ownerId, Long id);

    Optional<Station> findByIdAndDeletedAtIsNull(Long id);

    List<Station> findAllByDeletedAtIsNull();
}
