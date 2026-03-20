package com.project.evgo.booking.internal;

import com.project.evgo.booking.response.BookingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Booking entity to DTO.
 */
@Component
public class BookingDtoConverter {

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
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
