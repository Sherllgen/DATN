package com.project.evgo.station.request;

/**
 * Request DTO for updating a station photo's metadata.
 * Public API - accessible by other modules.
 */
public record UpdateStationPhotoRequest(
	String caption,
	Integer displayOrder) {
}
