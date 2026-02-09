package com.project.evgo.charger.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating a new port.
 * Note: chargerId is provided via PathVariable in the API endpoint.
 */
public record CreatePortRequest(
	@NotNull(message = "Port number is required") 
	@Positive(message = "Port number must be positive") 
	Integer portNumber) {
}
