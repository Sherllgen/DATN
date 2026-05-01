package com.project.evgo.booking.internal;

import com.project.evgo.booking.response.BookingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import com.project.evgo.station.StationService;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.user.VehicleService;
import com.project.evgo.user.response.VehicleResponse;
import lombok.RequiredArgsConstructor;

/**
 * Converter for Booking entity to DTO.
 */
@Component
@RequiredArgsConstructor
public class BookingDtoConverter {

    private final StationService stationService;
    private final ChargerService chargerService;
    private final VehicleService vehicleService;

    public BookingResponse toResponse(Booking booking) {
        BookingResponse response = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .stationId(booking.getStationId())
                .chargerId(booking.getChargerId())
                .vehicleId(booking.getVehicleId())
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
            if (booking.getVehicleId() != null) {
                try {
                    VehicleResponse vehicle = vehicleService.getVehicleById(booking.getVehicleId());
                    if (vehicle != null) {
                        response.setVehicleBrand(vehicle.getBrand());
                        response.setVehicleModelName(vehicle.getModelName());
                    }
                } catch (Exception ignored) {
                    // Ignored if vehicle is not found
                }
            }
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

    public List<BookingResponse> toResponseListBulk(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) return List.of();

        java.util.Set<Long> stationIds = bookings.stream().map(Booking::getStationId).collect(java.util.stream.Collectors.toSet());
        java.util.Set<Long> chargerIds = bookings.stream().map(Booking::getChargerId).collect(java.util.stream.Collectors.toSet());
        java.util.Set<Long> vehicleIds = bookings.stream().map(Booking::getVehicleId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());

        java.util.Map<Long, com.project.evgo.station.response.StationResponse> stationMap = stationService.findAllByIds(stationIds).stream()
                .collect(java.util.stream.Collectors.toMap(com.project.evgo.station.response.StationResponse::id, s -> s));
        java.util.Map<Long, com.project.evgo.charger.response.ChargerResponse> chargerMap = chargerService.findAllByIds(chargerIds).stream()
                .collect(java.util.stream.Collectors.toMap(com.project.evgo.charger.response.ChargerResponse::getId, c -> c));
        java.util.Map<Long, VehicleResponse> vehicleMap = vehicleService.findAllByIds(vehicleIds).stream()
                .collect(java.util.stream.Collectors.toMap(VehicleResponse::getId, v -> v));

        return bookings.stream()
                .map(b -> {
                    BookingResponse response = BookingResponse.builder()
                            .id(b.getId())
                            .userId(b.getUserId())
                            .stationId(b.getStationId())
                            .chargerId(b.getChargerId())
                            .vehicleId(b.getVehicleId())
                            .portNumber(b.getPortNumber())
                            .startTime(b.getStartTime())
                            .endTime(b.getEndTime())
                            .status(b.getStatus())
                            .totalPrice(b.getTotalPrice())
                            .createdAt(b.getCreatedAt())
                            .build();

                    com.project.evgo.station.response.StationResponse station = stationMap.get(b.getStationId());
                    if (station != null) {
                        response.setStationName(station.name());
                        response.setStationAddress(station.address());
                    }

                    com.project.evgo.charger.response.ChargerResponse charger = chargerMap.get(b.getChargerId());
                    if (charger != null) {
                        response.setChargerName(charger.getName());
                        response.setConnectorType(charger.getConnectorType());
                        response.setMaxPower(charger.getMaxPower());
                    }

                    if (b.getVehicleId() != null) {
                        VehicleResponse vehicle = vehicleMap.get(b.getVehicleId());
                        if (vehicle != null) {
                            response.setVehicleBrand(vehicle.getBrand());
                            response.setVehicleModelName(vehicle.getModelName());
                        }
                    }

                    return response;
                })
                .toList();
    }

    public Optional<BookingResponse> toResponse(Optional<Booking> booking) {
        return booking.map(this::toResponse);
    }
}
