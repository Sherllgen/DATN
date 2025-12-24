package com.project.evgo.complaint.internal;

import com.project.evgo.complaint.ComplaintService;
import com.project.evgo.complaint.response.ComplaintResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ComplaintService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintDtoConverter converter;

    @Override
    public Optional<ComplaintResponse> findById(Long id) {
        return converter.toResponse(complaintRepository.findById(id));
    }

    @Override
    public List<ComplaintResponse> findByUserId(Long userId) {
        return converter.toResponseList(complaintRepository.findByUserId(userId));
    }

    @Override
    public List<ComplaintResponse> findByStationId(Long stationId) {
        return converter.toResponseList(complaintRepository.findByStationId(stationId));
    }
}
