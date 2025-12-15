package com.project.evgo.charging.internal;

import com.project.evgo.charging.response.ChargingSessionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for ChargingSession entity to DTO.
 */
@Component
public class ChargingDtoConverter {

    public ChargingSessionResponse toResponse(ChargingSession session) {
        return ChargingSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .energyConsumed(session.getEnergyConsumed())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .build();
    }

    public List<ChargingSessionResponse> toResponseList(List<ChargingSession> sessions) {
        return sessions.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ChargingSessionResponse> toResponse(Optional<ChargingSession> session) {
        return session.map(this::toResponse);
    }
}
