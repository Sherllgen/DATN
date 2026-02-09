package com.project.evgo.charger.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new charger.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChargerRequest {

    @NotBlank(message = "Charger name is required")
    private String name;

    @NotNull(message = "Max power is required")
    @Positive(message = "Max power must be positive")
    private Double maxPower;

    @NotNull(message = "Station ID is required")
    private Long stationId;
}
