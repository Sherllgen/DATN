package com.project.evgo.booking.internal;

import com.project.evgo.booking.response.BookingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import com.project.evgo.station.StationService;
import com.project.evgo.charger.ChargerService;
import lombok.RequiredArgsConstructor;

/**
 * Converter for Booking entity to DTO.
 */
@Component
@RequiredArgsConstructor
public class BookingDtoConverter {

    private final StationService stationService;
    private final ChargerService chargerService;

    public BookingResponse toResponse(Booking booking) {
        BookingResponse response = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .stationId(booking.getStationId())
                .chargerId(booking.getChargerId())
                .portNumber(booking.getPortNumber())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt())
                .build();

        // Enrich with UI metadata
        try {
            stationService.findById(booking.getStationId()).ifPresent(station -> {
                response.setStationName(station.name());
                response.setStationAddress(station.address());
            });
            chargerService.findById(booking.getChargerId()).ifPresent(charger -> {
                response.setChargerName(charger.getName());
                response.setConnectorType(charger.getConnectorType());
                response.setMaxPower(charger.getMaxPower());
            });
        } catch (Exception e) {
            // Ignore errors to prevent failing the entire list serialization
        }

        return response;
    }

    public List<BookingResponse> toResponseList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<BookingResponse> toResponse(Optional<Booking> booking) {
        return booking.map(this::toResponse);
    }
}
