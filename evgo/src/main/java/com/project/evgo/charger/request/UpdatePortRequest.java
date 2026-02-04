package com.project.evgo.charger.request;

import com.project.evgo.sharedkernel.enums.PortStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating port status.
 */
public record UpdatePortRequest(
        @NotNull(message = "Port status is required") PortStatus status) {
}
