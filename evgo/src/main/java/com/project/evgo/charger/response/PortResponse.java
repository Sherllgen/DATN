package com.project.evgo.charger.response;

import com.project.evgo.sharedkernel.enums.PortStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for port information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortResponse {

    private Long id;
    private Integer portNumber;
    private PortStatus status;
    private Long chargerId;
    private LocalDateTime createdAt;
}
