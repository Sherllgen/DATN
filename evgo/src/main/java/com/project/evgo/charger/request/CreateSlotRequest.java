package com.project.evgo.charger.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new slot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSlotRequest {

    @NotNull(message = "Slot number is required")
    @Positive(message = "Slot number must be positive")
    private Integer slotNumber;

    @NotNull(message = "Charger ID is required")
    private Long chargerId;
}
