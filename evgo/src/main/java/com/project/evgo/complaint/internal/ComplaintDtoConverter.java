package com.project.evgo.complaint.internal;

import com.project.evgo.complaint.response.ComplaintResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Complaint entity to DTO.
 */
@Component
public class ComplaintDtoConverter {

    public ComplaintResponse toResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUserId())
                .stationId(complaint.getStationId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .adminNote(complaint.getAdminNote())
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .build();
    }

    public List<ComplaintResponse> toResponseList(List<Complaint> complaints) {
        return complaints.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ComplaintResponse> toResponse(Optional<Complaint> complaint) {
        return complaint.map(this::toResponse);
    }
}
