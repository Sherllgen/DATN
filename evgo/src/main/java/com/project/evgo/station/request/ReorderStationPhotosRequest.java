package com.project.evgo.station.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for reordering station photos.
 * Public API - accessible by other modules.
 */
public record ReorderStationPhotosRequest(

	@NotEmpty(message = "Photo IDs list cannot be empty") 
	List<Long> photoIdsInOrder) {
}
