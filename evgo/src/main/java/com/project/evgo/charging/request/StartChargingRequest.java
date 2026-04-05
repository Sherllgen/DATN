package com.project.evgo.charging.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for starting a charging session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartChargingRequest {

    private Long bookingId;

    @NotNull(message = "Port ID is required")
    private Long portId;
}
