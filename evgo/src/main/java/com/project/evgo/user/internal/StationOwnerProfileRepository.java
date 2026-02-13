package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationOwnerProfileRepository extends JpaRepository<StationOwnerProfile, Long> {
    @Query("SELECT s FROM StationOwnerProfile s WHERE (:status IS NULL OR s.status = :status)")
    Page<StationOwnerProfile> findAllByStatusOptionally(@Param("status") StationOwnerStatus status, Pageable pageable);

    Optional<StationOwnerProfile> findByRegistrationCode(String registrationCode);

    Optional<StationOwnerProfile> findByContactEmail(String contactEmail);

    Optional<StationOwnerProfile> findByUser(User user);
}
