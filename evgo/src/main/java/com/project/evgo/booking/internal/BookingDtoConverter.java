package com.project.evgo.booking.internal;

import com.project.evgo.booking.response.BookingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.project.evgo.station.StationService;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.user.UserService;
import com.project.evgo.user.VehicleService;
import com.project.evgo.user.response.UserResponse;
import com.project.evgo.user.response.VehicleResponse;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.booking.response.OwnerBookingSummaryResponse;
import java.util.Map;
import java.util.Set;
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
    private final UserService userService;

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

    public List<OwnerBookingSummaryResponse> toOwnerSummaryListBulk(
            List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty())
            return List.of();

        Set<Long> stationIds = bookings.stream().map(Booking::getStationId).collect(Collectors.toSet());
        Set<Long> userIds = bookings.stream().map(Booking::getUserId).filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, StationResponse> stationMap = stationService
                .findAllByIds(stationIds).stream()
                .collect(Collectors.toMap(StationResponse::id, s -> s));
        Map<Long, UserResponse> userMap = userService.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserResponse::id, u -> u));

        return bookings.stream()
                .map(b -> {
                    OwnerBookingSummaryResponse response = OwnerBookingSummaryResponse
                            .builder()
                            .id(b.getId())
                            .userId(b.getUserId())
                            .stationId(b.getStationId())
                            .status(b.getStatus())
                            .totalPrice(b.getTotalPrice())
                            .createdAt(b.getCreatedAt())
                            .build();

                    StationResponse station = stationMap.get(b.getStationId());
                    if (station != null) {
                        response.setStationName(station.name());
                    }

                    UserResponse user = userMap.get(b.getUserId());
                    if (user != null) {
                        response.setCustomerName(user.fullName());
                    }

                    return response;
                })
                .toList();
    }

    public Optional<BookingResponse> toResponse(Optional<Booking> booking) {
        return booking.map(this::toResponse);
    }
}
