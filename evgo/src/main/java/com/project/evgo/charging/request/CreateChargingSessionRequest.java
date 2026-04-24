package com.project.evgo.charging.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChargingSessionRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Port ID is required")
    private Long portId;

    private Long bookingId;
    
    private Integer transactionId;
}
