package com.project.evgo.charging.response;

import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for charging session information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSessionResponse {

    private Long id;
    private Long bookingId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal energyConsumed;
    private ChargingSessionStatus status;
    private LocalDateTime createdAt;
}
