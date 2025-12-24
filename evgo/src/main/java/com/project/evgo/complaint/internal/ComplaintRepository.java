package com.project.evgo.complaint.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Complaint entity.
 */
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserId(Long userId);

    List<Complaint> findByStationId(Long stationId);
}
