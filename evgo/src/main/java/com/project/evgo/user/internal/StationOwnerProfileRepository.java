package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationOwnerProfileRepository extends JpaRepository<StationOwnerProfile, Long> {
    Page<StationOwnerProfile> findByStatus(StationOwnerStatus status, Pageable pageable);
    Boolean existsByContactEmail(String contactEmail);
    Boolean existsByContactPhone(String contactPhone);
    Optional<StationOwnerProfile> findByContactEmail(String contactEmail);
}
