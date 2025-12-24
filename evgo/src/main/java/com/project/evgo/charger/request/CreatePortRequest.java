package com.project.evgo.charger.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new port.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePortRequest {

    @NotNull(message = "Port number is required")
    @Positive(message = "Port number must be positive")
    private Integer portNumber;

    @NotNull(message = "Charger ID is required")
    private Long chargerId;
}
