package com.project.evgo.complaint;

import com.project.evgo.complaint.response.ComplaintResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for complaint management.
 * Public API - accessible by other modules.
 */
public interface ComplaintService {

    Optional<ComplaintResponse> findById(Long id);

    List<ComplaintResponse> findByUserId(Long userId);

    List<ComplaintResponse> findByStationId(Long stationId);
}
