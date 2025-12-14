package com.project.evgo.charging.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for stopping a charging session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopChargingRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;
}
