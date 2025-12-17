package com.project.evgo.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Vehicle entity.
 * Internal - only used within User module.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findAllByUserId(Long userId);

    Optional<Vehicle> findByUserIdAndInUseTrue(Long userId);
}
