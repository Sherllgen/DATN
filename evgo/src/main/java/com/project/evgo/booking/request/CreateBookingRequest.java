package com.project.evgo.booking.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    @NotNull(message = "Station ID is required")
    private Long stationId;

    @NotNull(message = "Charger ID is required")
    private Long chargerId;

    @NotNull(message = "Port ID is required")
    private Long portId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
