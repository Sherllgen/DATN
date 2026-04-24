package com.project.evgo.charging.internal;

import com.project.evgo.charging.response.ChargingSessionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for ChargingSession entity to DTO.
 */
@Component
public class ChargingSessionDtoConverter {

    public ChargingSessionResponse convert(ChargingSession session) {
        return ChargingSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .portId(session.getPortId())
                .bookingId(session.getBookingId())
                .invoiceId(session.getInvoiceId())
                .transactionId(session.getTransactionId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .totalKwh(session.getTotalKwh())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    public List<ChargingSessionResponse> convert(List<ChargingSession> sessions) {
        return sessions.stream()
                .map(this::convert)
                .toList();
    }

    public Optional<ChargingSessionResponse> convert(Optional<ChargingSession> session) {
        return session.map(this::convert);
    }
}
